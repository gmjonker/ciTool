package gmjonker.citool.system;

import com.ibm.watson.developer_cloud.concept_insights.v2.ConceptInsights;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Corpus;
import gmjonker.citool.CiCorpusHelper;
import gmjonker.citool.CiCorpusQuerier;
import gmjonker.citool.CiUtil;
import gmjonker.citool.MatchedDocument;
import gmjonker.util.Util;

import java.util.List;

import static java.util.Arrays.asList;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class Explanations
{
    public static void main(String[] args)
    {
        // Given
        String ciUser       = Util.getEnvOrFail("CI_USER");
        String ciPassword   = Util.getEnvOrFail("CI_PASSWORD");
        String ciAccountId  = Util.getEnvOrFail("CI_ACCOUNT");
        String ciCorpusName = Util.getEnvOrDefault("CI_CORPUS", "Test");
        ConceptInsights conceptInsightsService = CiUtil.getConceptInsightsService(ciUser, ciPassword);
        Corpus corpus = CiCorpusHelper.getOrCreateCorpus(conceptInsightsService, ciAccountId, ciCorpusName, false);

        CiCorpusQuerier ciCorpusQuerier = new CiCorpusQuerier(conceptInsightsService, corpus);
        List<MatchedDocument> matchedDocuments = ciCorpusQuerier.searchDocumentsByConceptNames(asList("Chess", "Gardening"), 5);

        System.out.println(MatchedDocument.explain(matchedDocuments.get(0), conceptInsightsService, corpus));
        System.out.println();
        System.out.println(MatchedDocument.explain(matchedDocuments.get(1), conceptInsightsService, corpus));

    }

}
