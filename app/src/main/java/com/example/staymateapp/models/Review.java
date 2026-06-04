package com.example.staymateapp.models;

public class Review {

    private String id;          // 🔥 document id (optional but useful)
    private String userId;
    private String userName;
    private String propertyId;
    private String reviewText;
    private float rating;
    private long timestamp;

    // 🔥 EMPTY CONSTRUCTOR (REQUIRED FOR FIREBASE)
    public Review() {}

    // 🔥 CONSTRUCTOR
    public Review(String userId, String userName, String propertyId,
                  String reviewText, float rating, long timestamp) {
        this.userId = userId;
        this.userName = userName;
        this.propertyId = propertyId;
        this.reviewText = reviewText;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    // 🔥 GETTERS
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getPropertyId() { return propertyId; }
    public String getReviewText() { return reviewText; }
    public float getRating() { return rating; }
    public long getTimestamp() { return timestamp; }

    // 🔥 SETTERS
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setPropertyId(String propertyId) { this.propertyId = propertyId; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
    public void setRating(float rating) { this.rating = rating; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}