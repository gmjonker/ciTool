package gmjonker.citool;

import com.ibm.watson.developer_cloud.concept_insights.v2.ConceptInsights;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Corpus;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Document;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.DocumentProcessingStatus;

import java.util.Set;

class CiDocumentStatusQuerier
{
    private ConceptInsights conceptInsightsService;
    private Corpus corpus;

    CiDocumentStatusQuerier(ConceptInsights conceptInsightsService, Corpus corpus)
    {
        this.conceptInsightsService = conceptInsightsService;
        this.corpus = corpus;
    }

    void printAllStatuses()
    {
        Set<Document> documents = CiCorpusHelper.getDocuments(conceptInsightsService, corpus, CiCorpusHelper.NO_LIMIT);

        for (Document document : documents) {
            DocumentProcessingStatus state = conceptInsightsService.getDocumentProcessingState(document);
            System.out.printf("%-6.6s %s %s%n", document.getName(), state.getStatus(), state.getError());
        }
    }
}
