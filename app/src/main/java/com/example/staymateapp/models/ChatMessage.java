package com.example.staymateapp.models;

public class ChatMessage {

    private String message;
    private String status;
    private String senderId; // ✅ FIXED
    private String imageUrl;
    private long timestamp;

    public ChatMessage() {
        // Required for Firebase
    }

    public ChatMessage(String message, String senderId, String status) {
        this.message = message;
        this.senderId = senderId;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

    public String getMessage() {
        return message;
    }

    public String getSenderId() {   // ✅ THIS WAS MISSING
        return senderId;
    }

    public String getStatus() {
        return status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSenderId(String senderId) { // optional but good
        this.senderId = senderId;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}