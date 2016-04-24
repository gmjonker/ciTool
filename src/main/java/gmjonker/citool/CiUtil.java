package gmjonker.citool;

import com.ibm.watson.developer_cloud.concept_insights.v2.ConceptInsights;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Corpus;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Util;

@SuppressWarnings("WeakerAccess")
public class CiUtil
{
    private static final Logger log = LoggerFactory.getLogger(CiUtil.class);

    public static ConceptInsights getConceptInsightsService(String user, String password)
    {
        ConceptInsights conceptInsightsService = new ConceptInsights();
        conceptInsightsService.setUsernameAndPassword(user, password);
        return conceptInsightsService;
    }

    public static String getAccountId(ConceptInsights conceptInsights)
    {
        return conceptInsights.getFirstAccountId();
    }

    public static String getNameFromId(String id)
    {
        log.trace("id = " + id);
        int lastIndex = id.lastIndexOf('/');
        if (lastIndex == -1)
            throw new RuntimeException("Id '" + id + "' not a valid Watson id");
        String name = id.substring(lastIndex + 1);
        log.trace("name = " + name);
        return name;
    }

    //    public static String conceptNameToId(String conceptName)
    //    {
    //        return "/graphs/" + CiDefaults.GRAPH_ACCOUNT_ID + "/" + CiDefaults.GRAPH_NAME + "/concepts/" + conceptName;
    //    }

    public static Document getDocumentFromId(String documentId, Corpus corpus)
    {
        log.trace("documentId = " + documentId);
        int lastIndex = documentId.lastIndexOf('/');
        String name = documentId.substring(lastIndex + 1);
        log.trace("name = " + name);
        return new Document(corpus, name);
    }

    public static String urlToName(String url)
    {
        // Document name must satisfy [_\-\w\s]*, so basically only A-Z, a-z, 0-9, _ and - are allowed.
        //        try {
        //            return URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
        //        } catch (UnsupportedEncodingException e) {
        //            throw new RuntimeException(e);
        //        }
        String name = url.trim().toLowerCase();
        if (name.startsWith("https://")) name = name.substring(8);
        else if (name.startsWith("http://")) name = name.substring(7);
        if (name.endsWith("/")) name = name.substring(0, name.length() - 1);
        name = name.replaceAll("[^_\\-\\w\\s]+", "_");
        log.trace("{} -> {}", url, name);
        return name;
    }
}
