package com.example.staymateapp.models;

public class ChatUser {

    private String userId;
    private String name;
    private String profileImage;
    private String lastMessage;
    private Long timestamp;
    private Boolean online; // ✅ NEW

    public ChatUser() {}

    public ChatUser(String userId, String name, String profileImage, String lastMessage) {
        this.userId = userId;
        this.name = name;
        this.profileImage = profileImage;
        this.lastMessage = lastMessage;
    }

    public String getUserId() { return userId; }

    public String getName() { return name; }

    public String getProfileImage() { return profileImage; }

    public String getLastMessage() { return lastMessage; }

    // ✅ NEW METHODS
    public Boolean isOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}