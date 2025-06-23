package com.meekdev.clickly.core.models;

import java.util.UUID;

public class GroupMember {
    private final UUID playerId;
    private final String username;
    private final MemberRole role;
    private final long joinedAt;
    private final boolean isOnline;
    private final String currentServer;
    private final boolean isInVoice;

    public GroupMember(UUID playerId, String username, MemberRole role, long joinedAt, boolean isOnline, String currentServer, boolean isInVoice) {
        this.playerId = playerId;
        this.username = username;
        this.role = role;
        this.joinedAt = joinedAt;
        this.isOnline = isOnline;
        this.currentServer = currentServer;
        this.isInVoice = isInVoice;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getUsername() {
        return username;
    }

    public MemberRole getRole() {
        return role;
    }

    public long getJoinedAt() {
        return joinedAt;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public String getCurrentServer() {
        return currentServer;
    }

    public boolean isInVoice() {
        return isInVoice;
    }

    public enum MemberRole {
        CREATOR,
        ADMIN,
        MEMBER
    }
}