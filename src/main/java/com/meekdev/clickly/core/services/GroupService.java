package com.meekdev.clickly.core.services;

import com.meekdev.clickly.core.models.Group;
import com.meekdev.clickly.core.models.GroupMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface GroupService {
    CompletableFuture<Group> createGroup(String name, UUID creatorId);

    CompletableFuture<Boolean> joinGroup(String inviteCode, UUID playerId, String username);

    CompletableFuture<Boolean> leaveGroup(String groupId, UUID playerId);

    CompletableFuture<List<Group>> getPlayerGroups(UUID playerId);

    CompletableFuture<Optional<Group>> getGroup(String groupId);

    CompletableFuture<Boolean> deleteGroup(String groupId, UUID requesterId);

    CompletableFuture<Boolean> updateMemberStatus(String groupId, UUID memberId, boolean isOnline, String currentServer, boolean isInVoice);

    CompletableFuture<String> generateInviteCode(String groupId, UUID requesterId);

    Optional<Group> getCachedGroup(String groupId);

    List<Group> getCachedPlayerGroups(UUID playerId);
}