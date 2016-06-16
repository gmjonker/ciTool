package gmjonker.citool;

import com.ibm.watson.developer_cloud.concept_insights.v2.ConceptInsights;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.*;

import java.util.ArrayList;
import java.util.List;

import static gmjonker.math.GeneralMath.max;
import static gmjonker.math.GeneralMath.min;
import static gmjonker.math.GeneralMath.round;

/**
 * A document returned by Watson as a result of a query.
 */
@SuppressWarnings("WeakerAccess")
public class MatchedDocument extends Result
{
    public List<String> explanation;

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

    public static List<String> explain(MatchedDocument matchedDocument, ConceptInsights conceptInsightsService, Corpus corpus)
    {
        final int PADDING_TEXT_LENGTH = 30;

        Document document = CiCorpusHelper.getDocumentById(conceptInsightsService, corpus, matchedDocument.getId());

        List<String> fragments = new ArrayList<>();

        for (Annotation annotation : matchedDocument.getAnnotations())
        {
            Concept concept = annotation.getConcept();
            Double score = annotation.getScore();
            Integer partsIndex = annotation.getPartsIndex();
            List<Integer> textIndex = annotation.getTextIndex();

            String text = document.getParts().get(partsIndex).getData().replace('\n', ' ').replace('\t', ' ');

            Integer from = textIndex.get(0);
            Integer to = textIndex.get(1);
            fragments.add(
                    "... "
                    + text.substring(max(0, from - PADDING_TEXT_LENGTH), from)
                    + "["
                    + text.substring(from, to)
                    + "]["
                    + CiUtil.getNameFromId(concept.getId()) + ":" + round(score * 100, 2)
                    + "]"
                    + text.substring(to, min(to + PADDING_TEXT_LENGTH, text.length()))
                    + "..."
            );
        }

        return fragments;
    }
}
