package gmjonker.citool;

import java.util.Map;

/**
 * An abstraction of a document to be uploaded to Concept Insights.
 */
public class CiDocument
{
    public String name;  // The id of the document. Must be URL-safe.
    public String label; // Can be anything.
    public String body;  // For now, a single piece of plain text.
    public Map<String, String> userFields;

    // GroovyBean
    public CiDocument()
    {
    }

    public CiDocument(String name, String label, String body, Map<String, String> userFields)
    {
        this.name = name;
        this.label = label;
        this.body = body;
        this.userFields = userFields;
    }
}
