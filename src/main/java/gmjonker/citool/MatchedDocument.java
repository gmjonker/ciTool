package gmjonker.citool;

import com.ibm.watson.developer_cloud.concept_insights.v2.model.Annotation;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Result;

import java.util.List;

/**
 * A document returned by Watson as a result of a query.
 */
public class MatchedDocument extends Result
{
    public MatchedDocument(String id, String label, Double score, List<Annotation> annotations)
    {
        setId(id);
        setLabel(label);
        setScore(score);
        setAnnotations(annotations);
    }

    public String getName()
    {
        return CiUtil.getNameFromId(getId());
    }
}
