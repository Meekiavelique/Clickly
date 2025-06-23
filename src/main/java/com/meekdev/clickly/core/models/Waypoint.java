package com.meekdev.clickly.core.models;

import java.util.UUID;

public class Waypoint {
    private final String id;
    private final String name;
    private final int x;
    private final int y;
    private final int z;
    private final String dimension;
    private final int color;
    private final UUID creatorId;
    private final String groupId;
    private final long createdAt;
    private final boolean isPublic;

    public Waypoint(String id, String name, int x, int y, int z, String dimension, int color, UUID creatorId, String groupId, long createdAt, boolean isPublic) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.color = color;
        this.creatorId = creatorId;
        this.groupId = groupId;
        this.createdAt = createdAt;
        this.isPublic = isPublic;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
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

    public int getColor() {
        return color;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public String getGroupId() {
        return groupId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public double getDistanceFrom(double x, double y, double z) {
        double dx = this.x - x;
        double dy = this.y - y;
        double dz = this.z - z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}