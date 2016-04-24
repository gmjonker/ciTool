package gmjonker.citool;

import com.google.common.base.Stopwatch;
import com.ibm.watson.developer_cloud.concept_insights.v2.ConceptInsights;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Corpus;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.CorpusStats;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Document;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Part;
import util.LambdaLogger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static util.Util.map;
import static util.Util.nanosToString;

/**
 * Uploads documents, with URL in label.
 **/
public class CiDocumentUploader
{
    public static CiDocumentUploader getReplacingDocumentUploader(ConceptInsights conceptInsightsService, Corpus corpus)
    {
        return new CiDocumentUploader(conceptInsightsService, corpus, true, true);
    }

    private final ConceptInsights conceptInsightsService;
    private final Corpus corpus;
    private final boolean overwriteExisting;
    private final boolean deleteOthers;

    int numUploadedDocs = 0;
    int numDeletedDocs = 0;

    private static final LambdaLogger log = new LambdaLogger(CiDocumentUploader.class);
    private static Stopwatch mainStopwatch;

    public CiDocumentUploader(ConceptInsights conceptInsightsService, Corpus corpus, boolean overwriteExisting,
            boolean deleteOthers)
    {
        this.conceptInsightsService = conceptInsightsService;
        this.corpus = corpus;
        this.overwriteExisting = overwriteExisting;
        this.deleteOthers = deleteOthers;
    }

    //    public CiDocumentUploader(String user, String password, String accountId, String corpusName)
    //    {
    //        this(CiUtil.getConceptInsightsService(user, password),
    //                CiCorpusHelper.getCorpusWithRetries(CiUtil.getConceptInsightsService(user, password), accountId, corpusName));
    //    }

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

        // Convert ExtractedText to CI documents that can be added
        List<Document> documentsToAdd = new ArrayList<>();
        for (CiDocument ciDocument : ciDocuments)
        {
            Document document = new Document(corpus, ciDocument.name);
            document.setLabel(ciDocument.label);
            // Setting user fields doesn't work unfortunately
            //            Map<String, String> userFields = new HashMap<>();
            //            userFields.put("companyName", ciDocument.companyName);
            //            document.setUserFields(userFields);
            document.addParts(new Part("Text part", ciDocument.body, "text/plain"));
            log.trace("document id: {} ", document::getId);
            documentsToAdd.add(document);
        }

        // Determine which documents need to be deleted
        List<Document> documentsToDelete = new ArrayList<>();
        if (deleteOthers) {
            Set<String> namesToDelete = new HashSet<>();
            namesToDelete.addAll(allDocumentNames);
            namesToDelete.removeAll(map(ciDocuments, doc -> doc.name));
            log.debug("Documents to delete: {}", namesToDelete);
            for (String documentName : namesToDelete)
                documentsToDelete.add(new Document(corpus, documentName));
        }

        // Delete documents no longer in database from CI
        log.debug("Deleting {} documents from CI...", documentsToDelete.size());
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
                numDeletedDocs++;
            } catch (Exception e) {
                log.error("Error while deleting document {}", documentToDelete.getLabel(), e);
            }
        }

        // Add documents to CI
        log.debug("Adding {} documents to CI...", documentsToAdd.size());
        Stopwatch uploadStopwatch = Stopwatch.createStarted();
        for (int i = 0; i < documentsToAdd.size(); i++)
        {
            Document addedDocument = documentsToAdd.get(i);
            if (!overwriteExisting && allDocumentNames.contains(addedDocument.getName())) {
                log.debug("Document already in CI, skipping: {}", addedDocument.getName());
                log.trace(addedDocument.getLabel());
                continue;
            } else {
                //                    log.debug("Adding document {}/{}: {} - {}", i, documentsToAdd.size(), addedDocument.getName(),
                //                            addedDocument.getLabel());
                log.trace("Adding document: " + addedDocument);
                try {
                    Stopwatch stopwatch = Stopwatch.createStarted();
                    conceptInsightsService.createDocument(addedDocument);
                    stopwatch.stop();
                    log.debug("Added document {}/{}: {} - {} in {}", i, documentsToAdd.size(), addedDocument.getName(),
                            addedDocument.getLabel(), stopwatch);
                    //                        log.debug("Create document took {}", stopwatch.toString());
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
                    log.info("Remaining time: {}", nanosToString(expectedTimeRemaining));
                }
            }
        }

        log.trace("Fetching corpus stats...");
        CorpusStats corpusStats = conceptInsightsService.getCorpusStats(corpus);
        log.trace("CorpusStats:\n" + corpusStats);

        log.debug("Documents are all uploaded, and are now being processed by Watson...");
        log.info("Done.");
        return documentsToAdd;
    }

}
