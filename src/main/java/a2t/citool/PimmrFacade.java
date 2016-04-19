package a2t.citool;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import java.util.*;

public class PimmrFacade
{
    /**
     * Uploads all documents and waits until all have finished processing
     */
    public UploadResult uploadDocuments(Collection<PimmrDocument> pimmrDocuments)
    {

        // ...

        return new UploadResult();
    }

    /**
     * [Restaurant/tag scores]
     *
     * Queries CI to get relations between concepts and documents.
     * @return Table of all relations between documents and concepts.
     */
    public Table<String, PimmrDocument, Double> getRelatedConcepts(Collection<String> conceptNames)
    {

        // ...

        return HashBasedTable.create();
    }

    /**
     * (Future idea)
     * [Find restaurants that are like a given restaurant]
     *
     * For each of the given documents, get the N closest documents.
     * TYPE: would be nice to stay within type.
     */
    public Multimap<PimmrDocument, PimmrDocument> getNRelatedDocuments(Collection<PimmrDocument> pimmrDocuments, int n)
    {

        // ...

        return HashMultimap.create();
    }

    /**
     * (Future idea)
     * [Find restaurants by a given list of concepts]
     *
     * Get N documents that match best with given concepts.
     * TYPE: would be nice to get results for each type
     *
     * Note: this method would benefit from caching.
     */
    public Map<A2tDocument, Double> getDocumentsByConcepts(Collection<String> conceptNames, int n)
    {

        // ...

        return new HashMap<>();
    }
}
