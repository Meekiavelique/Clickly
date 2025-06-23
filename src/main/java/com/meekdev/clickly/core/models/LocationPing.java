package com.meekdev.clickly.core.models;

import java.util.UUID;

public class LocationPing {
    private final UUID senderId;
    private final String senderName;
    private final int x;
    private final int y;
    private final int z;
    private final String dimension;
    private final String server;
    private final long timestamp;
    private final String groupId;

    public LocationPing(UUID senderId, String senderName, int x, int y, int z, String dimension, String server, long timestamp, String groupId) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.server = server;
        this.timestamp = timestamp;
        this.groupId = groupId;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getDimension() {
        return dimension;
    }

    public String getServer() {
        return server;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getGroupId() {
        return groupId;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp > 30000;
    }
}