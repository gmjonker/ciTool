package gmjonker.citool;

import com.ibm.watson.developer_cloud.concept_insights.v2.ConceptInsights;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Corpus;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.CorpusProcessingState;
import util.LambdaLogger;

import static util.Util.simpleSleep;

public class CiStatus
{
    private static final LambdaLogger log = new LambdaLogger(CiStatus.class);

    static void checkProcessingState(ConceptInsights conceptInsightsService, Corpus corpus)
    {
        log.debug("Checking processing state...");

        log.debug("Corpus: {} ", corpus.getId());

        {
            if (log.isTraceEnabled()) {
                CorpusProcessingState corpusProcessingState = conceptInsightsService.getCorpusProcessingState(corpus);
                log.trace("corpusProcessingState = " + corpusProcessingState);
            }
        }

        {
            log.trace("Fetching document processing states...");
            boolean allReady = false;
            while ( ! allReady) {
                CorpusProcessingState corpusProcessingState = logProcessingState(conceptInsightsService, corpus);
                //                if (log.isTraceEnabled()) {
                //                    allReady = true;
                //                    for (Document addedDocument : addedDocuments) {
                ////                      log.trace("addedDocument.getId() = " + addedDocument.getId());
                ////                      log.trace("addedDocument.getName() = " + addedDocument.getName());
                ////                      log.trace("addedDocument.getLabel() = " + addedDocument.getLabel());
                ////                      log.trace("addedDocument.getExpiresOn() = " + addedDocument.getExpiresOn());
                //                        DocumentProcessingStatus documentProcessingState = conceptInsightsService.getDocumentProcessingState(addedDocument);
                ////                      log.trace("documentProcessingState = " + documentProcessingState);
                //                        String status = documentProcessingState.getStatus();
                ////                      log.trace("documentProcessingState.getStatus() = " + status);
                //                        boolean ready = Objects.equals(status, "ready");
                ////                      log.trace("ready = " + ready);
                //                        if (ready) {
                //                            log.debug("Document ready     : {}", addedDocument.getLabel());
                //                        } else {
                //                            allReady = false;
                //                            log.debug("Document processing: {}", addedDocument.getLabel());
                //                        }
                //                    }
                //                }
                if (corpusProcessingState.getBuildStatus().getProcessing() == 0)
                    allReady = true;
                if ( ! allReady)
                    simpleSleep(10000);
            }
            log.debug("");
            log.debug("All documents have finished processing.");
            CorpusProcessingState corpusProcessingState = conceptInsightsService.getCorpusProcessingState(corpus);
            log.debug("Corpus processing state: " + corpusProcessingState);

                //            Documents documents = conceptInsightsService.listDocuments(corpus, new HashMap<>());
                //            log.debug("Corpus {} contains {} documents ", corpus.getName(), documents.getDocuments().size());
        }
    }

    public static CorpusProcessingState logProcessingState(ConceptInsights conceptInsightsService, Corpus corpus)
    {
        CorpusProcessingState corpusProcessingState = conceptInsightsService.getCorpusProcessingState(corpus);
        log.trace("corpusProcessingState = " + corpusProcessingState);
        log.debug("Ready: {}, processing: {}, error: {}", corpusProcessingState.getBuildStatus().getReady(),
                corpusProcessingState.getBuildStatus().getProcessing(), corpusProcessingState.getBuildStatus().getError());
        return corpusProcessingState;
    }
}
