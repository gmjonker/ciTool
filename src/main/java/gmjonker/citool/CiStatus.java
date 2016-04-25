package gmjonker.citool;

import com.ibm.watson.developer_cloud.concept_insights.v2.ConceptInsights;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Corpus;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.CorpusProcessingState;
import util.LambdaLogger;

import static util.Util.simpleSleep;

public class CiStatus
{
    private static final LambdaLogger log = new LambdaLogger(CiStatus.class);

    /**
     * Checks the status of documents in a given corpus. Will return after all documents are ready or failed.
     */
    public static void waitUntilAllDocumentsFinished(ConceptInsights conceptInsightsService, Corpus corpus)
    {
        log.info("Checking processing state of corpus {}...", corpus.getId());

        boolean allReady = false;
        while (!allReady) {
            CorpusProcessingState corpusProcessingState = conceptInsightsService.getCorpusProcessingState(corpus);
            log.debug("Ready: {}, processing: {}, error: {}", corpusProcessingState.getBuildStatus().getReady(),
                    corpusProcessingState.getBuildStatus().getProcessing(), corpusProcessingState.getBuildStatus().getError());
            if (corpusProcessingState.getBuildStatus().getProcessing() == 0)
                allReady = true;
            if (!allReady)
                simpleSleep(10000);
        }

        log.info("All documents have finished processing.");
    }

    public static void logProcessingState(ConceptInsights conceptInsightsService, Corpus corpus)
    {
        CorpusProcessingState corpusProcessingState = conceptInsightsService.getCorpusProcessingState(corpus);
        log.debug("Ready: {}, processing: {}, error: {}", corpusProcessingState.getBuildStatus().getReady(),
                corpusProcessingState.getBuildStatus().getProcessing(), corpusProcessingState.getBuildStatus().getError());
    }
}
