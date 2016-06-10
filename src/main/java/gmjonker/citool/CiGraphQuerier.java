package gmjonker.citool;

import com.ibm.watson.developer_cloud.concept_insights.v2.ConceptInsights;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.*;
import com.ibm.watson.developer_cloud.service.NotFoundException;
import com.ibm.watson.developer_cloud.service.ServiceResponseException;
import gmjonker.util.LambdaLogger;

import java.util.*;

import static gmjonker.citool.CiUtil.getNameFromId;
import static gmjonker.util.CollectionsUtil.map;

@SuppressWarnings("WeakerAccess")
public class CiGraphQuerier
{
    private static CiGraphQuerier instance = null;

    private static final LambdaLogger log = new LambdaLogger(CiGraphQuerier.class);

    public static CiGraphQuerier getInstance()
    {
        if (instance == null) {
            String ciUser = "7b3844eb-be73-4e7d-9422-afa656b97dc9";
            String ciPassword = "4eNotr1BjuQH";
            instance = new CiGraphQuerier(ciUser, ciPassword);
        }
        return instance;
    }

    ///

    private ConceptInsights conceptInsightsService;

    public CiGraphQuerier(String ciUser, String ciPassword)
    {
        conceptInsightsService = new ConceptInsights();
        conceptInsightsService.setUsernameAndPassword(ciUser, ciPassword);
    }

    public void showGraphs()
    {
        Graphs graphs = conceptInsightsService.listGraphs();
        for (String graph : graphs.getGraphs()) {
            System.out.println("graph = " + graph);
        }
    }

    public boolean checkConceptNameExists(Graph graph, String conceptName)
    {
        try {
            ConceptMetadata conceptMetadata = conceptInsightsService.getConcept(new Concept(graph, conceptName));
            log.debug("Concept '{}' found in graph '{}'", conceptName, graph.getName());
            log.debug("ConceptMetadata: {}", conceptMetadata);
            return true;
        } catch (NotFoundException e) {
            log.debug("Concept '{}' not found in graph '{}'", conceptName, graph.getName());
        } catch (ServiceResponseException e) {
            log.error("Something went wrong while looking for concept '{}' in graph '{}'", conceptName, graph.getName(), e);
        }
        return false;
    }

    private Set<String> conceptNamesFound = new HashSet<>();
    private Set<String> conceptNamesNotFound = new HashSet<>();

    public boolean checkConceptNameExistsCached(Graph graph, String conceptName)
    {
        if (conceptNamesFound.contains(conceptName))
            return true;
        if (conceptNamesNotFound.contains(conceptName))
            return false;
        try {
            ConceptMetadata conceptMetadata = conceptInsightsService.getConcept(new Concept(graph, conceptName));
            log.debug("Concept '{}' found in graph '{}'", conceptName, graph.getName());
            log.trace("ConceptMetadata: {}", conceptMetadata);
            conceptNamesFound.add(conceptName);
            return true;
        } catch (NotFoundException e) {
            log.warn("Concept '{}' not found in graph '{}'", conceptName, graph.getName());
            conceptNamesNotFound.add(conceptName);
        } catch (ServiceResponseException e) {
            log.error("Something went wrong while looking for concept '{}' in graph '{}'", conceptName, graph.getName(), e);
        }
        return false;
    }

    // If byPrefix false, then 'chess' only matches 'chess'. If true, then it also matches 'chess master'.
    public List<Concept> findConceptsByLabel(String label, boolean byPrefix, int howMany)
    {
        log.trace("Searching concepts by label: {}", label);
        final Map<String, Object> params = new HashMap<>();
        params.put(ConceptInsights.QUERY, label);
        params.put(ConceptInsights.PREFIX, byPrefix);
        params.put(ConceptInsights.LIMIT, howMany);
        Matches matches = conceptInsightsService.searchGraphsConceptByLabel(Graph.WIKIPEDIA, params);
        List<Concept> matchedConcepts = matches.getMatches();
        List<Concept> concepts = new ArrayList<>();
        for (Concept concept : matchedConcepts) {
            log.trace("concept = " + concept);
            log.debug("Matched concept: {} ({})", concept.getLabel(), concept.getId());
            concepts.add(new Concept(concept.getId(), concept.getLabel()));
        }
        //        for (Match match : matches.getMatches()) {
        //            log.trace("concept = " + concept);
        //            log.debug("Matched concept: {} ({})", concept.getLabel(), concept.getId());
        //            concepts.add(new Concept(concept.getId(), concept.getLabel()));
        //        }
        return concepts;
    }

    public List<ScoredConcept> identifyConceptsInText(String text)
    {
        log.trace("Annotating: '{}'", text);
        Annotations annotations = conceptInsightsService.annotateText(Graph.WIKIPEDIA, text);
        List<ScoredConcept> scoredConcepts = annotations.getAnnotations();
        Collections.sort(scoredConcepts, (sc1, sc2) -> - Double.compare(sc1.getScore(), sc2.getScore()));
        log.trace("Found the following concepts: {}", scoredConcepts);
        return scoredConcepts;
    }

    /**
     * @param level How unpopular are the concepts allowed to be. 1 = popular, 3 = unpopular.
     */
    void getConceptRelatedConcepts(String conceptId, Integer level, Integer limit)
    {
        final Map<String, Object> parameters = new HashMap<>();
        if (level != null) parameters.put(ConceptInsights.LEVEL, level);
        if (limit != null) parameters.put(ConceptInsights.LIMIT, limit);
        Concepts conceptRelatedConcepts = conceptInsightsService.getConceptRelatedConcepts(conceptId, parameters);
        List<ScoredConcept> concepts = conceptRelatedConcepts.getConcepts();
        for (int i = 0; i < concepts.size(); i++) {
            ScoredConcept scoredConcept = concepts.get(i);
            log.debug("Related concept #{} from {} is {} with score {}.", i, getNameFromId(conceptId),
                    scoredConcept.getConcept().getLabel(), scoredConcept.getScore());
        }
    }

    void getConceptRelatedConcepts(Concept concept)
    {
        final Map<String, Object> parameters = new HashMap<>();
        Concepts conceptRelatedConcepts = conceptInsightsService.getConceptRelatedConcepts(concept, parameters);
        for (ScoredConcept scoredConcept : conceptRelatedConcepts.getConcepts()) {
            System.out.println("scoredConcept = " + scoredConcept);
        }
    }

    Scores getGraphRelationScores(String conceptId, List<String> conceptIds)
    {
        Scores scores = conceptInsightsService.getGraphRelationScores(conceptId, conceptIds);
        for (Score score : scores.getScores())
            log.trace("score = " + score);
        String conceptName = getNameFromId(conceptId);
        log.trace("Relation scores between {} and {} are {}", conceptName, map(conceptIds, CiUtil::getNameFromId),
                map(scores.getScores(), Score::getScore));
        return scores;
    }

    Scores getGraphRelationScores(Concept concept, List<String> conceptIds)
    {
        Scores scores = conceptInsightsService.getGraphRelationScores(concept, conceptIds);
        for (Score score : scores.getScores())
            log.trace("score = " + score);
        log.trace("Relation scores between {} and {} are {}", concept.getName(), map(conceptIds, CiUtil::getNameFromId),
                map(scores.getScores(), Score::getScore));
        return scores;
    }
}
