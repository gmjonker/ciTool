package gmjonker.citool;

public class CiDocument
{
    public String name;  // The id of the document. Must be URL-safe.
    public String label; // Can be anything.
    public String body;  // For now, a single piece of plain text.

    public CiDocument()
    {
    }

    public CiDocument(String name, String label, String body)
    {
        this.name = name;
        this.label = label;
        this.body = body;
    }
}
