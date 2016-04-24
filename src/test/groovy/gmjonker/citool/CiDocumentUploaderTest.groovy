package gmjonker.citool

import com.ibm.watson.developer_cloud.concept_insights.v2.ConceptInsights
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Corpus
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Documents
import spock.lang.Specification

class CiDocumentUploaderTest extends Specification
{
    def "UploadDocuments"()
    {
        given:
        def corpus = Mock(Corpus)
        corpus.getId() >> "/corpora/myAccount/myCorpus"

        def conceptInsights = Mock(ConceptInsights)
        conceptInsights.listDocuments(corpus, _) >> new Documents(documents: [ "/corpora/myAccount/myCorpus/documents/myDocId" ])
        conceptInsights.deleteDocument(_) >> { args -> println "ConceptInsights: Received request to delete document " + args[0].getId() }
        conceptInsights.createDocument(_) >> { args -> println "ConceptInsights: Received request to create document " + args[0].getId() }
        conceptInsights.getCorpusStats(corpus) >> { args -> println "ConceptInsights: Received request to get corpus stats" }

        def documentUploader = CiDocumentUploader.getReplacingDocumentUploader(conceptInsights, corpus);

        when:
        documentUploader.uploadDocuments( [ new CiDocument(name: "name", label: "label", body: "body") ] );

        then:
        noExceptionThrown()
    }
}
