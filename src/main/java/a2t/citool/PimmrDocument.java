package a2t.citool;

public class PimmrDocument
{
    enum Type { name, reviews, blogs }

    public final Integer itemId;
    public final Type type;
    public final String text;

    String name;  // URL-encoded id, e.g. itemId-documentType
    String label; // City-name

    public PimmrDocument(Integer itemId, Type type, String text, String city, String name)
    {
        this.itemId = itemId;
        this.type = type;
        this.text = text;
    }
}
