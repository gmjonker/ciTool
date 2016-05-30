package gmjonker.citool;

import com.ibm.watson.developer_cloud.concept_insights.v2.ConceptInsights;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.*;
import com.ibm.watson.developer_cloud.service.InternalServerErrorException;
import gmjonker.util.LambdaLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ibm.watson.developer_cloud.alchemy.v1.util.AlchemyEndPoints.AlchemyAPI.concepts;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/*
    The label field of the document object is specially indexed for prefix searching using the
    /corpora/{account_id}/{corpus}/label_search method. This method is available for low latency
    retrieval of document labels within a corpus. Among other things, it is suitable for auto-complete
    function of an (web/mobile) application. For this reason, the recommendation is to use the label field
    with a user friendly string.
 */
@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class CiCorpusQuerier
{
    private ConceptInsights conceptInsightsService;
    private Corpus corpus;

    private static final LambdaLogger log = new LambdaLogger(CiCorpusQuerier.class);

    public CiCorpusQuerier(ConceptInsights conceptInsightsService, Corpus corpus)
    {
        this.conceptInsightsService = conceptInsightsService;
        this.corpus = corpus;
    }

    public List<MatchedDocument> findDocumentsByConceptNames(List<String> conceptNames, Object limit)
    {
        final List<String> ids = new ArrayList<>();
        for (String conceptName : conceptNames) {
            ids.add(CiUtil.conceptNameToId(Graph.WIKIPEDIA.getId(), conceptName));
        }
        return findDocumentsByConceptIds(ids, limit);
    }

    List<MatchedDocument> findDocumentsByConceptIds(List<String> conceptIds, Object limit)
    {
        log.debug("Searching documents by conceptIds...");
        log.debug("conceptIds = " + conceptIds);
        final Map<String, Object> params = new HashMap<>();
        params.put(ConceptInsights.IDS, conceptIds);
        params.put(ConceptInsights.LIMIT, limit);
        log.trace("params = " + params);
        try {
            QueryConcepts queryConcepts = conceptInsightsService.conceptualSearch(corpus, params);
            for (Concept concept : queryConcepts.getQueryConcepts()) {
                log.debug("Query concept = " + concept.getLabel());
            }
            List<Result> results = queryConcepts.getResults();
            List<MatchedDocument> matchedDocuments = new ArrayList<>();
            for (Result result : results) {
                log.trace("result = " + result);
                log.debug("Matched document result: {}", result.getLabel());
                matchedDocuments.add(new MatchedDocument(result.getId(), result.getLabel(), result.getScore(),
                        result.getAnnotations()));

            }
            log.debug("Conceptual search return {} results on concept matches {}", matchedDocuments.size(), concepts);
            return matchedDocuments;
        } catch (InternalServerErrorException e) {
            log.error("Internal server error while finding documents by concept", e);
            // System.out.println("e.getResponse() = " + e.getResponse());
            return asList(new MatchedDocument("Watson internal server error", "Watson interal server error", 0.0, emptyList()));
        }
    }
}
