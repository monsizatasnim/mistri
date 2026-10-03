package domain.entity;

public class Reference {
    private String id;
    private String providerId;
    private String referenceName;
    private String contactInfo;
    private String description;

    public Reference(String id, String providerId, String referenceName, String contactInfo, String description) {
        this.id = id;
        this.providerId = providerId;
        this.referenceName = referenceName;
        this.contactInfo = contactInfo;
        this.description = description;
    }

    public String getId() { return id; }
    public String getProviderId() { return providerId; }
    public String getReferenceName() { return referenceName; }
    public String getContactInfo() { return contactInfo; }
    public String getDescription() { return description; }
}
