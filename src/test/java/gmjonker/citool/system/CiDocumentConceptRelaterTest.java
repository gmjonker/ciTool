package gmjonker.citool.system;

import com.google.common.collect.Table;
import com.ibm.watson.developer_cloud.concept_insights.v2.ConceptInsights;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Concept;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Corpus;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Graph;
import gmjonker.citool.CiCorpusHelper;
import gmjonker.citool.CiDocumentConceptRelater;
import gmjonker.citool.CiUtil;
import gmjonker.util.Util;

import java.io.IOException;
import java.util.ArrayList;

public class CiDocumentConceptRelaterTest
{
    // System test that calls Watson, don't include as build test.
    private static void testWithPimmrTestCorpus() throws IOException
    {
        // Given
        String ciUser       = Util.getEnvOrFail("CI_USER");
        String ciPassword   = Util.getEnvOrFail("CI_PASSWORD");
        String ciAccountId  = Util.getEnvOrFail("CI_ACCOUNT_ID");
        String ciCorpusName = Util.getEnvOrDefault("CI_CORPUS_NAME", "Test");
        ConceptInsights conceptInsightsService = CiUtil.getConceptInsightsService(ciUser, ciPassword);
        Corpus corpus = CiCorpusHelper.getOrCreateCorpus(conceptInsightsService, ciAccountId, ciCorpusName, false);
        CiDocumentConceptRelater ciDocumentConceptRelater = new CiDocumentConceptRelater(conceptInsightsService, corpus);
        ArrayList<Concept> concepts = new ArrayList<>();
        Graph graph = new Graph("wikipedia", "en-20120601");
        concepts.add(new Concept(graph, "Cafeteria"));
        concepts.add(new Concept(graph, "Coffeehouse"));
        concepts.add(new Concept(graph, "Wine"));

        // When
        Table<String, String, Double> allDocumentConceptRelations = ciDocumentConceptRelater.getAllDocumentConceptRelations(concepts);

        // Then we get this
        System.out.println("allDocumentConceptRelations = " + allDocumentConceptRelations);
    }

    public static void main(String[] args) throws IOException
    {
        testWithPimmrTestCorpus();
    }
}
