package gmjonker.citool;

import com.ibm.watson.developer_cloud.concept_insights.v2.ConceptInsights;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Accounts;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Corpus;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Document;
import gmjonker.util.LambdaLogger;

@SuppressWarnings("WeakerAccess")
public class CiUtil
{
    private static final LambdaLogger log = new LambdaLogger(CiUtil.class);

    public static ConceptInsights getConceptInsightsService(String user, String password)
    {
        ConceptInsights conceptInsightsService = new ConceptInsights();
        conceptInsightsService.setUsernameAndPassword(user, password);
        return conceptInsightsService;
    }

    public static void getAccountsInfo(ConceptInsights conceptInsightsService)
    {
        Accounts accounts = conceptInsightsService.getAccountsInfo();
        log.info("accounts = " + accounts);
        String accountId = accounts.getAccounts().get(0).getId();
        log.info("first accountId = " + accountId);
    }

    public static String getAccountId(ConceptInsights conceptInsights)
    {
        return conceptInsights.getFirstAccountId();
    }

    public static String getNameFromId(String id)
    {
        int lastIndex = id.lastIndexOf('/');
        if (lastIndex == -1)
            throw new RuntimeException("Id '" + id + "' not a valid Watson id");
        return id.substring(lastIndex + 1);
    }

    public static String conceptNameToId(String accountId, String graphName, String conceptName)
    {
        return "/graphs/" + accountId + "/" + graphName + "/concepts/" + conceptName;
    }

    public static String conceptNameToId(String graphId, String conceptName)
    {
        return graphId + "/concepts/" + conceptName;
    }

    public static Document getDocumentFromId(String documentId, Corpus corpus)
    {
        int lastIndex = documentId.lastIndexOf('/');
        String name = documentId.substring(lastIndex + 1);
        return new Document(corpus, name);
    }

    /**
     * Encodes an URL such that it can be used as CI document name.
     */
    public static String urlToDocumentName(String url)
    {
        // Document name must satisfy [_\-\w\s]*, so basically only A-Z, a-z, 0-9, _ and - are allowed.
        String name = url.trim().toLowerCase();
        if (name.startsWith("https://")) name = name.substring(8);
        else if (name.startsWith("http://")) name = name.substring(7);
        if (name.endsWith("/")) name = name.substring(0, name.length() - 1);
        name = name.replaceAll("[^_\\-\\w\\s]+", "_");
        log.trace("{} -> {}", url, name);
        return name;
    }
}
