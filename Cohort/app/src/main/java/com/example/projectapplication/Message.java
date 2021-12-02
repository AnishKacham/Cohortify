package com.example.projectapplication;

public class Message {
    public String id, senderId, groupId, timestamp, message, userId, joinLeave;
    public Boolean isEdited;

    public Message() {}

    public Message(String userId, String groupId, String joinLeave, String timestamp) {
        this.userId = userId;
        this.groupId = groupId;
        this.joinLeave = joinLeave;
        this.timestamp = timestamp;
    }

    public Message(String id, String senderId, String groupId, String timestamp, String message) {
        this.id = id;
        this.senderId = senderId;
        this.groupId = groupId;
        this.timestamp = timestamp;
        this.message = message;
        this.isEdited = false;
    }
}
