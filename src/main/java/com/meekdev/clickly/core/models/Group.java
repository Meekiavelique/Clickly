package com.meekdev.clickly.core.models;

import java.util.List;
import java.util.UUID;

public class Group {
    private final String id;
    private final String name;
    private final UUID creatorId;
    private final List<GroupMember> members;
    private final long createdAt;
    private final String inviteCode;

    public Group(String id, String name, UUID creatorId, List<GroupMember> members, long createdAt, String inviteCode) {
        this.id = id;
        this.name = name;
        this.creatorId = creatorId;
        this.members = members;
        this.createdAt = createdAt;
        this.inviteCode = inviteCode;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public List<GroupMember> getMembers() {
        return members;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public boolean isCreator(UUID playerId) {
        return creatorId.equals(playerId);
    }

    public int getMemberCount() {
        return members.size();
    }
}