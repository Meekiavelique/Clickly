package com.meekdev.clickly.core.models;

import java.util.UUID;

public class ChatMessage {
    private final UUID senderId;
    private final String senderName;
    private final String content;
    private final long timestamp;
    private final String groupId;
    private final MessageType type;

    public ChatMessage(UUID senderId, String senderName, String content, long timestamp, String groupId, MessageType type) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.timestamp = timestamp;
        this.groupId = groupId;
        this.type = type;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getGroupId() {
        return groupId;
    }

    public MessageType getType() {
        return type;
    }

    public enum MessageType {
        NORMAL,
        SYSTEM,
        ANNOUNCEMENT
    }
}