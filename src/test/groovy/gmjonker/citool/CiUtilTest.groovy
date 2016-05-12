package gmjonker.citool

import spock.lang.Specification

public class CiUtilTest extends Specification
{
    def "UploadDocuments"()
    {
        expect:
        CiUtil.urlToDocumentName("https://this.is.a/url") == "this_is_a_url"
    }
}
