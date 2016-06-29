package gmjonker.citool;

import com.ibm.watson.developer_cloud.concept_insights.v2.ConceptInsights;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Corpus;
import gmjonker.citool.domain.MatchedDocument;
import gmjonker.util.Util;

import java.util.List;

import static java.util.Arrays.asList;

public class CiCorpusQuerierTest
{
    public static void searchDocumentsByConceptNames() throws Exception
    {
        // Given
        String ciUser = Util.getEnvOrFail("CI_USER");
        String ciPassword = Util.getEnvOrFail("CI_PASSWORD");
        String ciAccountId = Util.getEnvOrFail("CI_ACCOUNT");
        String ciCorpusName = Util.getEnvOrDefault("CI_CORPUS", "Test");
        ConceptInsights conceptInsightsService = CiUtil.getConceptInsightsService(ciUser, ciPassword);
        Corpus corpus = CiCorpusHelper.getOrCreateCorpus(conceptInsightsService, ciAccountId, ciCorpusName, false);

        CiCorpusQuerier ciCorpusQuerier = new CiCorpusQuerier(conceptInsightsService, corpus);
        List<MatchedDocument> chess = ciCorpusQuerier.searchDocumentsByConceptNames(asList("Chess"), 3);

        System.out.println("chess = " + chess);
    }

    public static void main(String[] args) throws Exception
    {
        searchDocumentsByConceptNames();
    }

}