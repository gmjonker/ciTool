package gmjonker.citool;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.ibm.watson.developer_cloud.concept_insights.v2.ConceptInsights;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.*;
import gmjonker.citool.util.LambdaLogger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
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
     * @return A table with the document names in the rows, the concept ids/names??? in the columns, and the relations
     *     in the cells.
     */
    public Table<String, String, Double> getAllDocumentConceptRelations(List<Concept> concepts) throws IOException
    {
        CSVPrinter csvPrinter = new CSVPrinter(new FileWriter("documentConceptRelations.csv"), CSVFormat.EXCEL);

        Set<Document> allDocuments = CiCorpusHelper.getAllDocuments(conceptInsightsService, corpus);

        Table<String, String, Double> documentConceptRelations = HashBasedTable.create();

        csvPrinter.print("");
        for (Concept concept : concepts)
            csvPrinter.print(concept.getId());
        csvPrinter.println();

        int count = 0;
        for (Document document : allDocuments)
        {
            String documentName = document.getName();
            log.debug("Getting concept relations for doc {}/{}: {}", count, allDocuments.size(), documentName);
            // Do concepts in small chunks, otherwise Watson goes boom.
            List<List<Concept>> lists = Lists.partition(concepts, 40);
            for (List<Concept> conceptsSubList : lists) {
                try {
                    log.trace("conceptsSubList = {}", conceptsSubList);
                    Scores scores = conceptInsightsService.getDocumentRelationScores(document, conceptsSubList);
                    for (Score score : scores.getScores())
                        documentConceptRelations.put(documentName, score.getConcept(), score.getScore());
                    System.out.print(".");
                } catch (Exception e) {
                    log.error("Could not get relations for document {}", documentName, e);
                }
            }
            System.out.println();
            csvPrinter.print(documentName);
            for (Concept concept : concepts) {
                Double aDouble = documentConceptRelations.get(documentName, concept.getId());
                csvPrinter.print(aDouble);
            }
            csvPrinter.println();
            csvPrinter.flush();
            count++;
        }
        csvPrinter.close();

        log.trace("documentConceptRelations = {}", documentConceptRelations);

        return documentConceptRelations;
    }
}
