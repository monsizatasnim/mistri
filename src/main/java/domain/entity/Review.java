package domain.entity;

public class Review {
    private String id;
    private String serviceId;
    private String customerName;
    private int rating;
    private String comment;

    public Review(String id, String serviceId, String customerName, int rating, String comment) {
        this.id = id;
        this.serviceId = serviceId;
        this.customerName = customerName;
        this.rating = rating;
        this.comment = comment;
    }

    public String getId() { return id; }
    public String getServiceId() { return serviceId; }
    public String getCustomerName() { return customerName; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
}
