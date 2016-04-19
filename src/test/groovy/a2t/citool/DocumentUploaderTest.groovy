package a2t.citool

import com.ibm.watson.developer_cloud.concept_insights.v2.ConceptInsights
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Corpus
import com.ibm.watson.developer_cloud.concept_insights.v2.model.Documents
import spock.lang.Specification

class DocumentUploaderTest extends Specification
{
    def "UploadDocuments"()
    {
        given:
        def corpus = Mock(Corpus)
        corpus.getId() >> "/corpora/myAccount/myCorpus"

        def conceptInsights = Mock(ConceptInsights)
        conceptInsights.listDocuments(corpus, _) >> new Documents(documents: [ "/corpora/myAccount/myCorpus/documents/myDocId" ])

        def documentUploader = new DocumentUploader(conceptInsights, corpus);

        when:
        documentUploader.uploadDocuments( [ new CiDocument(name: "name", label: "label", body: "body") ] , true, true);

        then:
        noExceptionThrown()
    }
}
