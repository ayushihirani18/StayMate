package com.example.staymateapp.models;

import java.util.List;

public class Booking {

    private String propertyName;
    private String rent;
    private String status;

    private String userName;
    private long timestamp;

    private String id;

    // 🔥 NEW FIELDS (IMPORTANT)
    private String location;
    private String contact;
    private String ownerId;
    private String userId;
    String propertyId;

    private List<String> imageUrls;

    public Booking() {}

    public Booking(String propertyName, String rent, String status) {
        this.propertyName = propertyName;
        this.rent = rent;
        this.status = status;
    }

    public String getPropertyName() { return propertyName; }
    public String getRent() { return rent; }
    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getUserName() { return userName; }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public void setUserName(String userName) { this.userName = userName; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    // 🔥 NEW GETTERS/SETTERS
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
}