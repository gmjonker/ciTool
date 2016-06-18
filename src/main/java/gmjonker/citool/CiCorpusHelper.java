package gmjonker.citool;

import com.ibm.watson.developer_cloud.concept_insights.v2.ConceptInsights;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Corpora;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Corpus;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Document;
import com.ibm.watson.developer_cloud.service.BadRequestException;
import gmjonker.util.LambdaLogger;

import java.util.*;
import java.util.stream.Collectors;

import static gmjonker.util.CollectionsUtil.map;
import static gmjonker.util.StringNormalization.containsNormalized;
import static java.util.Collections.emptySet;

public class CiCorpusHelper
{
    public static final int NO_LIMIT = -1;

    private static final LambdaLogger log = new LambdaLogger(CiCorpusHelper.class);

    public static void showCorpora(ConceptInsights conceptInsightsService, String accountId)
    {
        Corpora corpora = conceptInsightsService.listCorpora(accountId);
        log.info("Account has {} corpora: {}", corpora.getCorpora().size(), map(corpora.getCorpora(), Corpus::getId));
    }

    public static Corpus createCorpus(ConceptInsights conceptInsightsService, String accountId, String corpusName)
    {
        log.info("Creating new corpus {}/{}", accountId, corpusName);
        Corpus corpus = new Corpus(accountId, corpusName);
        conceptInsightsService.createCorpus(corpus);
        log.info("Corpus created");
        return corpus;
    }

    public static Corpus getCorpus(ConceptInsights conceptInsightsService, String accountId, String corpusName)
    {
        return conceptInsightsService.getCorpus(new Corpus(accountId, corpusName));
    }

    public static Corpus getOrCreateCorpus(ConceptInsights conceptInsightsService, String accountId, String corpusName,
            boolean interactive)
    {
        try
        {
            return getCorpus(conceptInsightsService, accountId, corpusName);
        }
        catch (BadRequestException e) {
            if (Objects.equals(e.getMessage(), "not found")) {
                log.info("Corpus '{}' does not exist, will now create", corpusName);
                if (interactive) {
                    log.info("Are you sure? [yN]:");
                    Scanner scanner = new Scanner(System.in);
                    String line = scanner.nextLine().toLowerCase();
                    if (!line.startsWith("y")) {
                        log.info("Aborting");
                        System.exit(0);
                    }
                }
                Corpus corpus = createCorpus(conceptInsightsService, accountId, corpusName);
                log.info("Corpus '{}' created", corpus.getName());
                return corpus;
            }
            throw e;
        }
    }

    @Deprecated
    public static Set<String> getAllDocumentIds(ConceptInsights conceptInsightsService, Corpus corpus)
    {
        log.debug("Getting all documents from corpus '{}'...", CiUtil.getNameFromId(corpus.getId()));
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ConceptInsights.LIMIT, 0); // 0 will get the maximum of 100.000 documents
        List<String> documentIds = conceptInsightsService.listDocuments(corpus, parameters).getDocuments();
        log.trace("documentIds = {}", () -> documentIds);
        if (documentIds.size() == 100000)
            log.warn("Received 100000 documents from CI. This means that there are probablye more than 100000 documents," +
                    "so we should fetch documents incrementally.");
        return new HashSet<>(documentIds);
    }

    /**
     * @param limit Limit, or -1 for no limit.
     */
    public static Set<String> getDocumentIds(ConceptInsights conceptInsightsService, Corpus corpus, int limit)
    {
        return getDocumentIds(conceptInsightsService, corpus, limit, emptySet());
    }

    /**
     * @param limit Limit, or -1 for no limit.
     * @param onlyIds Only process these documents.
     */
    public static Set<String> getDocumentIds(ConceptInsights conceptInsightsService, Corpus corpus, int limit,
            Collection<String> onlyIds)
    {
        if (onlyIds.isEmpty())
            log.debug("Getting {} documents from corpus '{}'...", limit == NO_LIMIT ? "all" : limit,
                    CiUtil.getNameFromId(corpus.getId()));
        else
            log.debug("Getting documents {} from corpus '{}'...", onlyIds, CiUtil.getNameFromId(corpus.getId()));

        Map<String, Object> parameters = new HashMap<>();
        if (limit == NO_LIMIT)
            parameters.put(ConceptInsights.LIMIT, 0); // 0 will get the maximum of 100.000 documents
        else
            parameters.put(ConceptInsights.LIMIT, limit); // 0 will get the maximum of 100.000 documents
        List<String> documentIds = conceptInsightsService.listDocuments(corpus, parameters).getDocuments();
        if ( ! onlyIds.isEmpty())
            documentIds.retainAll(onlyIds);
        log.trace("documentIds = {}", () -> documentIds);
        if (documentIds.size() == 100000)
            log.warn("Received 100000 documents from CI. This means that there are probablye more than 100000 documents," +
                    "so we should fetch documents incrementally.");
        return new HashSet<>(documentIds);
    }

    @Deprecated
    public static Set<String> getAllDocumentNames(ConceptInsights conceptInsightsService, Corpus corpus)
    {
        return map(getAllDocumentIds(conceptInsightsService, corpus), CiUtil::getNameFromId);
    }

    public static Document getDocumentById(ConceptInsights conceptInsightsService, Corpus corpus, String documentId)
    {
        return conceptInsightsService.getDocument(new Document(corpus, CiUtil.getNameFromId(documentId)));
    }

    @Deprecated
    public static Set<Document> getAllDocuments(ConceptInsights conceptInsightsService, Corpus corpus)
    {
        return map(getAllDocumentIds(conceptInsightsService, corpus), id -> CiUtil.getDocumentFromId(id, corpus));
    }

    /**
     * @param limit Limit, or -1 for no limit.
     */
    public static Set<Document> getDocuments(ConceptInsights conceptInsightsService, Corpus corpus, int limit)
    {
        return getDocuments(conceptInsightsService, corpus, limit, emptySet());
    }

    /**
     * @param limit Limit, or -1 for no limit.
     * @param onlyIds Only get these documents.
     */
    public static Set<Document> getDocuments(ConceptInsights conceptInsightsService, Corpus corpus, int limit,
            Collection<String> onlyIds)
    {
        return map(getDocumentIds(conceptInsightsService, corpus, limit, onlyIds), id -> CiUtil.getDocumentFromId(id, corpus));
    }

    public static List<Document> findDocumentsByPartialName(ConceptInsights conceptInsightsService, Corpus corpus, String query)
    {
        return getDocumentIds(conceptInsightsService, corpus, NO_LIMIT).stream()
                .filter(
                        id -> containsNormalized(CiUtil.getNameFromId(id), query)
                )
                .map(
                        id -> CiUtil.getDocumentFromId(id, corpus)
                )
                .collect(Collectors.toList());
    }

    static void populate(ConceptInsights conceptInsightsService, Document document)
    {
        Document fullDocument = conceptInsightsService.getDocument(document);
        assert Objects.equals(document.getId(), fullDocument.getId());
        assert Objects.equals(document.getName(), fullDocument.getName());
        document.setExpiresOn(fullDocument.getExpiresOn());
        document.setLabel(fullDocument.getLabel());
        document.setLastModified(fullDocument.getLastModified());
        document.setParts(fullDocument.getParts());
        document.setTimeToLive(fullDocument.getTimeToLive());
        document.setUserFields(fullDocument.getUserFields());
    }
}
