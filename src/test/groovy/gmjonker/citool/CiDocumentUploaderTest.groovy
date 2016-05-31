package gmjonker.citool

import com.ibm.watson.developer_cloud.concept_insights.v2.ConceptInsights
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Corpus
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Documents
import spock.lang.Specification

class CiDocumentUploaderTest extends Specification
{
    def "UploadDocuments"()
    {
        given: "We have a Concept Insights service"
        def corpus = Mock(Corpus)
        corpus.getId() >> "/corpora/myAccount/myCorpus"
        def conceptInsights = Mock(ConceptInsights)
        conceptInsights.listDocuments(corpus, _) >> new Documents(documents: [ "/corpora/myAccount/myCorpus/documents/myDocId" ])
        conceptInsights.deleteDocument(_) >> { args -> println "ConceptInsights: Received request to delete document " + args[0].getId() }
        conceptInsights.createDocument(_) >> { args -> println "ConceptInsights: Received request to create document " + args[0].getId() }
        conceptInsights.getCorpusStats(corpus) >> { args -> println "ConceptInsights: Received request to get corpus stats" }
        def documentUploader = CiDocumentUploader.getReplacingDocumentUploader(conceptInsights, corpus, false, true);

        when: "We upload documents"
        documentUploader.uploadDocuments( [ new CiDocument(name: "name", label: "label", body: "body") ] );

        then: "No errors are thrown"
        noExceptionThrown()
    }

    def "Skip uploading empty documents"()
    {
        given: "We have a Concept Insights service"
        def corpus = Mock(Corpus)
        corpus.getId() >> "/corpora/myAccount/myCorpus"
        def conceptInsights = Mock(ConceptInsights)
        conceptInsights.listDocuments(corpus, _) >> new Documents(documents: [ "/corpora/myAccount/myCorpus/documents/myDocId" ])
        conceptInsights.deleteDocument(_) >> { args -> println "ConceptInsights: Received request to delete document " + args[0].getId() }
        conceptInsights.createDocument(_) >> { args -> println "ConceptInsights: Received request to create document " + args[0].getId() }
        conceptInsights.getCorpusStats(corpus) >> { args -> println "ConceptInsights: Received request to get corpus stats" }
        def documentUploader = CiDocumentUploader.getReplacingDocumentUploader(conceptInsights, corpus, false, true);

        when: "We upload empty document"
        documentUploader.uploadDocuments( [ new CiDocument(name: "name", label: "label", body: "") ] );

        then: "No errors are thrown"
        noExceptionThrown()
    }
}
