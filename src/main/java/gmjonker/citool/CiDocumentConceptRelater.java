package gmjonker.citool;

import com.google.common.collect.*;
import com.ibm.watson.developer_cloud.concept_insights.v2.ConceptInsights;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.*;
import gmjonker.util.LambdaLogger;
import lombok.Cleanup;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static gmjonker.util.CollectionsUtil.filter;
import static gmjonker.util.CollectionsUtil.map;
import static java.util.Collections.emptySet;

/**
 * Queries Watson to find relations between all documents in a corpus and a given list of concepts.
 */
public class CiDocumentConceptRelater
{
    public static final int NO_LIMIT = -1;
    public static final int CONCEPT_BATCH_SIZE = 20;

    private final ConceptInsights conceptInsightsService;
    private final Corpus corpus;
    private final boolean writeFailedRequests;

    private static final LambdaLogger log = new LambdaLogger(CiDocumentConceptRelater.class);

    public CiDocumentConceptRelater(ConceptInsights conceptInsightsService, Corpus corpus, boolean writeFailedRequests)
            throws IOException
    {
        this.conceptInsightsService = conceptInsightsService;
        this.corpus = corpus;
        this.writeFailedRequests = writeFailedRequests;
    }

    /**
     * Determines the relations between a given set of concepts and all the documents in the corpus.
     * @param limit Limit on the number of documents processed
     * @return A table with the document names in the rows, the concept names in the columns, and the relations
     *     in the cells.
     */
    public Table<String, String, Double> getDocumentConceptNameRelations(List<Concept> concepts, int limit) throws IOException
    {
        return getDocumentConceptNameRelations(concepts, limit, emptySet());
    }

    /**
     * Determines the relations between a given set of concepts and all the documents in the corpus.
     * @param limit Limit on the number of documents processed
     * @param onlyIds Only process these documents
     * @return A table with the document names in the rows, the concept names in the columns, and the relations
     *     in the cells.
     */
    public Table<String, String, Double> getDocumentConceptNameRelations(List<Concept> concepts, int limit,
            Collection<String> onlyIds) throws IOException
    {
        log.trace("concepts = {}", concepts);
        log.trace("onlyIds = {}", onlyIds);

        Set<Document> documents = CiCorpusHelper.getDocuments(conceptInsightsService, corpus, limit, onlyIds);
        Table<String, String, Double> documentConceptRelations = HashBasedTable.create();
        @Cleanup CSVPrinter failedRequestsPrinter = null;
        try {
            failedRequestsPrinter = new CSVPrinter(new FileWriter("failedRequests.csv"), CSVFormat.EXCEL);
        } catch (IOException e) {
            log.warn("Could not initialize failedRequestsPrinter");
        }

        concepts = filter(concepts,
                (Concept concept) -> {
                    if (CiUtil.CONCEPT_NAMES_IN_WIKIPEDIA_BUT_NOT_IN_WATSON.contains(concept.getName())) {
                        log.warnOnce("Concept '{}' not known by Watson, skipping...", concept.getName());
                        return false;
                    } else {
                        return true;
                    }
                }
        );
        log.trace("Filtered concepts: {}", concepts);

        int count = 0;
        Multiset<Integer> successfulConceptCounts = HashMultiset.create();
        for (Document document : documents)
        {
            String documentName = document.getName();
            log.debug("Getting concept relations for doc {}/{}: '{}' ({})", count + 1, documents.size(), documentName,
                    document.getLabel());
            // Do concepts in small chunks, otherwise Watson goes boom.
            List<List<Concept>> lists = Lists.partition(concepts, CONCEPT_BATCH_SIZE);
            System.out.println("\b\b");
            List<Concept> successfulConcepts = new ArrayList<>();
            List<Concept> failedConcepts = new ArrayList<>();
            for (List<Concept> conceptsSubList : lists) {
                try {
                    Scores scores = conceptInsightsService.getDocumentRelationScores(document, conceptsSubList);
                    for (Score score : scores.getScores())
                        documentConceptRelations.put(documentName, CiUtil.getNameFromId(score.getConcept()), score.getScore());
                    System.out.print(".");
                    successfulConcepts.addAll(conceptsSubList);
                } catch (Exception e) {
                    log.error("Could not get relations for document {}, concepts {}", documentName,
                            map(conceptsSubList, Concept::getName), e);
                    failedConcepts.addAll(conceptsSubList);
                }
            }
            System.out.println();
            count++;
            if (failedConcepts.size() > 0 && failedRequestsPrinter != null) {
                try {
                    failedRequestsPrinter.printRecord(documentName,
                            map(failedConcepts, concept -> CiUtil.getNameFromId(concept.getId())));
                    failedRequestsPrinter.flush();
                } catch (IOException e1) {
                    log.error("Could not write failed concepts to file", e1);
                }
            }
            // stats.addValue(successfulConcepts.size());
            successfulConceptCounts.add(successfulConcepts.size());
        }

        log.trace("documentConceptRelations = {}", documentConceptRelations);
        log.debug("Succesful concept counts: {}", successfulConceptCounts);

        return documentConceptRelations;
    }
}
