package gmjonker.citool;

import com.ibm.watson.developer_cloud.concept_insights.v2.ConceptInsights;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Corpus;
import gmjonker.util.Util;

public class CiDocumentStatusQuerierTest
{
    // System test that calls Watson, don't include as build test.
    private static void testWithPimmrTestCorpus()
    {
        // Given
        String ciUser = Util.getEnvOrFail("CI_USER");
        String ciPassword = Util.getEnvOrFail("CI_PASSWORD");
        String ciAccountId = Util.getEnvOrFail("CI_ACCOUNT");
        String ciCorpusName = Util.getEnvOrDefault("CI_CORPUS", "Test");
        ConceptInsights conceptInsightsService = CiUtil.getConceptInsightsService(ciUser, ciPassword);
        Corpus corpus = CiCorpusHelper.getOrCreateCorpus(conceptInsightsService, ciAccountId, ciCorpusName, false);

        CiDocumentStatusQuerier documentStatusQuerier = new CiDocumentStatusQuerier(conceptInsightsService, corpus);

        // When
        documentStatusQuerier.printAllStatuses();
    }

    public static void main(String[] args)
    {
        testWithPimmrTestCorpus();
    }
}