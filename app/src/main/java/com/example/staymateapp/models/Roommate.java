package com.example.staymateapp.models;

public class Roommate {

    private String id;
    private String userId;
    private String title;
    private String description;
    private String city;
    private String area;
    private String budget;
    private String date;
    private long timestamp;

    public Roommate() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCity() { return city; }
    public String getArea() { return area; }
    public String getBudget() { return budget; }
    public String getDate() { return date; }

    public long getTimestamp() { return timestamp; }
}