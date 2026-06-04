package com.example.staymateapp.models;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class Property implements Serializable {

    @DocumentId
    private String id;   // Firestore document ID

    private String name;
    private String location;
    private int minRent;
    private int maxRent;
    private String contact;
    private String type;
    private String gender;
    private String ownerId;
    private String propertyType;

    public String getPropertyType() { return propertyType; }
    public void setPropertyType(String propertyType) { this.propertyType = propertyType; }
    private String imageUrl; // fallback for old data
    private Map<String, Boolean> amenities = new HashMap<>();
    private List<String> imageUrls = new ArrayList<>();

    // Required empty constructor for Firestore
    public Property() {
    }

    // 🔹 Getters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public String getLocation() {
        return location != null ? location : "";
    }

    public int getMinRent() {
        return minRent;
    }

    public int getMaxRent() {
        return maxRent;
    }

    public String getContact() {
        return contact != null ? contact : "";
    }

    public String getType() {
        return type != null ? type : "";
    }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Map<String, Boolean> getAmenities() {
        return amenities != null ? amenities : new HashMap<>();
    }

    public List<String> getImageUrls() {
        return imageUrls != null ? imageUrls : new ArrayList<>();
    }

    public String getImageUrl() {
        return imageUrl != null ? imageUrl : "";
    }
    // 🔹 Helper methods

    public boolean hasWifi() {
        return Boolean.TRUE.equals(amenities.get("wifi"));
    }

    public boolean hasAc() {
        return Boolean.TRUE.equals(amenities.get("ac"));
    }

    public boolean hasParking() {
        return Boolean.TRUE.equals(amenities.get("parking"));
    }
}