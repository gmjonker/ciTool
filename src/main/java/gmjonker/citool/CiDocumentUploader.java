package gmjonker.citool;

import com.google.common.base.Stopwatch;
import com.ibm.watson.developer_cloud.concept_insights.v2.ConceptInsights;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Corpus;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Document;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Part;
import gmjonker.util.LambdaLogger;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static gmjonker.util.CollectionsUtil.map;
import static gmjonker.util.FormattingUtil.nanosToString;

/**
 * Uploads documents to Concept Insights.
 **/
public class CiDocumentUploader
{
    public static CiDocumentUploader getReplacingDocumentUploader(ConceptInsights conceptInsightsService, Corpus corpus,
            boolean interactive, boolean noDelete)
    {
        return new CiDocumentUploader(conceptInsightsService, corpus, true, ! noDelete, true, interactive);
    }

    private final ConceptInsights conceptInsightsService;
    private final Corpus corpus;
    private final boolean overwriteExisting;
    private final boolean deleteOthers;
    private final boolean skipUploadEmptyDocuments;
    private final boolean interactive;

    private static final LambdaLogger log = new LambdaLogger(CiDocumentUploader.class);

    public CiDocumentUploader(ConceptInsights conceptInsightsService, Corpus corpus, boolean overwriteExisting,
            boolean deleteOthers, boolean skipUploadEmptyDocuments, boolean interactive)
    {
        this.conceptInsightsService = conceptInsightsService;
        this.corpus = corpus;
        this.overwriteExisting = overwriteExisting;
        this.deleteOthers = deleteOthers;
        this.skipUploadEmptyDocuments = skipUploadEmptyDocuments;
        this.interactive = interactive;
    }

    /**
     * @return The documents that were added.
     */
    public List<Document> uploadDocuments(List<CiDocument> ciDocuments)
    {
        log.info("CI document uploader is uploading to corpus {}", corpus.getId());
        log.debug("Overwrite existing documents set to: {}", overwriteExisting);
        log.debug("Delete other documents set to: {}", deleteOthers);

        // Get names of documents currently in CI
        Set<String> allDocumentNames = CiCorpusHelper.getAllDocumentNames(conceptInsightsService, corpus);
        log.info("There are currently {} documents in CI", allDocumentNames.size());
        log.trace("allDocumentNames = {}", allDocumentNames);

        // Convert ciDocuments to documents that can be added
        List<Document> documentsToAdd = new ArrayList<>();
        for (CiDocument ciDocument : ciDocuments)
        {
            Document document = new Document(corpus, ciDocument.name);
            document.setLabel(ciDocument.label);
            // This should set the user fields, but doesn't seem to work somehow
            // Map<String, String> userFields = new HashMap<>();
            // userFields.put("companyName", ciDocument.companyName);
            // document.setUserFields(userFields);
            document.addParts(new Part("Text part", ciDocument.body, "text/plain"));
            if (ciDocument.userFields != null && ! ciDocument.userFields.isEmpty())
                document.setUserFields(ciDocument.userFields);
            log.trace("document id: {} ", document::getId);
            documentsToAdd.add(document);
        }

        // Determine which documents need to be deleted
        List<Document> documentsToDelete = new ArrayList<>();
        if (deleteOthers) {
            Set<String> namesToDelete = new HashSet<>();
            namesToDelete.addAll(allDocumentNames);
            namesToDelete.removeAll(map(ciDocuments, doc -> doc.name));
            log.debug("Document ids to delete: {}", namesToDelete);

            if ( ! namesToDelete.isEmpty() && interactive) {
                System.out.println("Continue? [Yn]");
                Scanner scanner = new Scanner(System.in);
                String answer = scanner.nextLine().toLowerCase();
                if (!Objects.equals(answer, "y") && ! Objects.equals(answer, "")) {
                    System.out.println("Exiting.");
                    System.exit(-1);
                }
            }

            for (String documentName : namesToDelete)
                documentsToDelete.add(new Document(corpus, documentName));
        }

        // Delete documents no longer in database from CI
        log.info("Deleting {} documents from CI...", documentsToDelete.size());
        for (int i = 0; i < documentsToDelete.size(); i++)
        {
            Document documentToDelete = documentsToDelete.get(i);
            if ( ! allDocumentNames.contains(documentToDelete.getName())) {
                log.debug("Document not in CI, skipping: {}", documentToDelete.getName());
                log.trace(documentToDelete.getLabel());
                continue;
            }
            log.trace("Deleting document: " + documentToDelete);
            try {
                Stopwatch stopwatch = Stopwatch.createStarted();
                conceptInsightsService.deleteDocument(documentToDelete);
                stopwatch.stop();
                log.debug("Deleted document {}/{}: {} - {} in {}", i, documentsToDelete.size(), documentToDelete.getName(),
                        documentToDelete.getLabel(), stopwatch);
            } catch (Exception e) {
                log.error("Error while deleting document {}", documentToDelete.getLabel(), e);
            }
        }

        // Add documents to CI
        log.info("Adding {} documents to CI...", documentsToAdd.size());
        int numUploadedDocs = 0;
        Stopwatch uploadStopwatch = Stopwatch.createStarted();
        for (int i = 0; i < documentsToAdd.size(); i++)
        {
            Document addedDocument = documentsToAdd.get(i);
            if (!overwriteExisting && allDocumentNames.contains(addedDocument.getName())) {
                log.debug("Document already in CI, skipping: {}", addedDocument.getName());
                log.trace(addedDocument.getLabel());
                continue;
            } else {
                long size = addedDocument.getParts().stream().mapToLong(part -> part.getData().length()).sum();
                if (skipUploadEmptyDocuments && size == 0) {
                    log.debug("Document '{}' empty, skipping.", addedDocument.getName());
                    continue;
                }
                log.trace("Adding document: " + addedDocument);
                try {
                    Stopwatch stopwatch = Stopwatch.createStarted();
                    conceptInsightsService.createDocument(addedDocument);
                    stopwatch.stop();
                    log.debug("Added document {}/{}: {} - {} ({} kb) in {}", i, documentsToAdd.size(), addedDocument.getName(),
                            addedDocument.getLabel(), size / 1000, stopwatch);
                    numUploadedDocs++;
                } catch (Exception e) {
                    log.error("Error while uploading document {}", addedDocument.getLabel(), e);
                }
            }
            if (numUploadedDocs % 25 == 0) {
                // Log some stats
                CiStatus.logProcessingState(conceptInsightsService, corpus);
                if (numUploadedDocs > 0) {
                    long elapsed = uploadStopwatch.elapsed(TimeUnit.NANOSECONDS);
                    long averageNanosPerUploadSoFar = elapsed / numUploadedDocs;
                    long expectedTimeRemaining = averageNanosPerUploadSoFar * (documentsToAdd.size() - numUploadedDocs);
                    log.debug("Remaining time: {}", nanosToString(expectedTimeRemaining));
                }
            }
        }

        log.trace("Corpus stats: {}", () -> conceptInsightsService.getCorpusStats(corpus));

        log.info("Documents are all uploaded, and are now being processed by Watson...");
        return documentsToAdd;
    }
}
