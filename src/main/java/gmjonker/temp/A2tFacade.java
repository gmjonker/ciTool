package gmjonker.temp;

import gmjonker.citool.UploadResult;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class A2tFacade
{
    /**
     * Uploads all documents and waits until all have finished processing.
     */
    public UploadResult uploadDocuments(Collection<PimmrDocument> pimmrDocuments)
    {

        // ...

        return new UploadResult();
    }

    /**
     * [Interest search, (matching companies with card answers)].
     *
     * <p>Get N documents that match best with given concepts.
     * TYPE: would be nice to get results for each type
     *
     * <p>Note: this method would benefit from caching.
     */
    public Map<A2tDocument, Double> getDocumentsByConcepts(Collection<String> conceptNames, int n)
    {

        // ...

        return new HashMap<>();
    }

    /**
     * [Matching companies with card answers].
     *
     * <p>For each concept tuple, get N documents that match best with given concepts.
     * TYPE: would be nice to get results for each type
     */
    public Map<Collection<String>, Map<A2tDocument, Double>> getDocumentsByConceptsLists(
            Collection<Collection<String>> conceptNamesLists, int n)
    {

        // ...

        return new HashMap<>();
    }

    /**
     * [Matching companies with card answers].
     *
     * <p>For each concept tuple, get relation scores with all documents in corpus.
     * TYPE: would be nice to get results for each type
     */
    public Map<Collection<String>, Map<A2tDocument, Double>> getDocumentsByConceptsLists(
            Collection<Collection<String>> conceptNamesLists)
    {

        // ...

        return new HashMap<>();
    }
}
