package com.example.chatter.models;

import java.util.Date;

public class ChatMessage {
    private String id;
    private String userId;
    private String username;
    private String displayName;
    private String profilePic;
    private String lastMessage;
    private Date dateObject;

    public ChatMessage(String id, String userId, String username, String displayName, String profilePic, String lastMessage, Date date) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.profilePic = profilePic;
        this.lastMessage = lastMessage;
        this.dateObject = date;
    }

    // Getters and setters for the properties

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Date getDateObject() {
        return dateObject;
    }

    public void setDateObject(Date dateObject) {
        this.dateObject = dateObject;
    }
}
