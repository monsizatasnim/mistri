package domain.entity;

public class ShopOwner {
    private String id;
    private String shopName;
    private String location;
    private String contractInfo;
    private String ownerName;
    private String email;
    private boolean isApproved;

    public ShopOwner(String id, String shopName, String location, String contractInfo, String ownerName, String email, boolean isApproved) {
        this.id = id;
        this.shopName = shopName;
        this.location = location;
        this.contractInfo = contractInfo;
        this.ownerName = ownerName;
        this.email = email;
        this.isApproved = isApproved;
    }

    public String getId() { return id; }
    public String getShopName() { return shopName; }
    public String getLocation() { return location; }
    public String getContractInfo() { return contractInfo; }
    public String getOwnerName() { return ownerName; }
    public String getEmail() { return email; }
    public boolean isApproved() { return isApproved; }

    public void setApproved(boolean approved) { isApproved = approved; }
}
