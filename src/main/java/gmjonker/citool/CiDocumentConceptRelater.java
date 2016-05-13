package gmjonker.citool;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.ibm.watson.developer_cloud.concept_insights.v2.ConceptInsights;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.*;
import gmjonker.util.LambdaLogger;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Queries Watson to find relations between all documents in a corpus and a given list of concepts.
 */
public class CiDocumentConceptRelater
{

    private final ConceptInsights conceptInsightsService;
    private final Corpus corpus;

    private static final LambdaLogger log = new LambdaLogger(CiDocumentConceptRelater.class);

    public CiDocumentConceptRelater(ConceptInsights conceptInsightsService, Corpus corpus)
    {
        this.conceptInsightsService = conceptInsightsService;
        this.corpus = corpus;
    }

    /**
     * Determines the relations between a given set of concepts and all the documents in the corpus.
     * @return A table with the document names in the rows, the concept ids in the columns, and the relations
     *     in the cells.
     */
    public Table<String, String, Double> getAllDocumentConceptRelations(List<Concept> concepts) throws IOException
    {
        Set<Document> allDocuments = CiCorpusHelper.getAllDocuments(conceptInsightsService, corpus);
        Table<String, String, Double> documentConceptRelations = HashBasedTable.create();

        int count = 0;
        for (Document document : allDocuments)
        {
            String documentName = document.getName();
            log.debug("Getting concept relations for doc {}/{}: '{}'", count, allDocuments.size(), documentName);
            // Do concepts in small chunks, otherwise Watson goes boom.
            List<List<Concept>> lists = Lists.partition(concepts, 40);
            for (List<Concept> conceptsSubList : lists) {
                try {
                    Scores scores = conceptInsightsService.getDocumentRelationScores(document, conceptsSubList);
                    for (Score score : scores.getScores())
                        documentConceptRelations.put(documentName, score.getConcept(), score.getScore());
                    System.out.print(".");
                } catch (Exception e) {
                    log.error("Could not get relations for document {}", documentName, e);
                }
            }
            System.out.println();
            count++;
        }

        log.trace("documentConceptRelations = {}", documentConceptRelations);

        return documentConceptRelations;
    }
}
