package com.meekdev.clickly.client.services;

import com.meekdev.clickly.Clickly;
import com.meekdev.clickly.core.ServiceManager;
import com.meekdev.clickly.core.services.GroupService;
import com.meekdev.clickly.core.services.NetworkService;
import com.meekdev.clickly.core.models.Group;
import com.meekdev.clickly.core.models.GroupMember;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class GroupServiceImpl implements GroupService {
    private final Map<String, Group> cachedGroups = new ConcurrentHashMap<>();
    private final Map<UUID, List<Group>> playerGroups = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Group> createGroup(String name, UUID creatorId) {
        NetworkService networkService = ServiceManager.getInstance().require(NetworkService.class);
        CompletableFuture<Group> future = new CompletableFuture<>();

        if (!networkService.isConnected()) {
            future.completeExceptionally(new RuntimeException("Not connected to server"));
            return future;
        }

        JSONObject data = new JSONObject();
        data.put("name", name);
        data.put("creator_id", creatorId.toString());

        // This would need a proper HTTP client implementation
        // For now, simulate group creation
        String groupId = UUID.randomUUID().toString();
        Group group = new Group(groupId, name, creatorId, new ArrayList<>(), System.currentTimeMillis(), UUID.randomUUID().toString().substring(0, 8));
        cachedGroups.put(group.getId(), group);
        updatePlayerGroups(creatorId, group);
        future.complete(group);

        return future;
    }

    @Override
    public CompletableFuture<Boolean> joinGroup(String inviteCode, UUID playerId, String username) {
        NetworkService networkService = ServiceManager.getInstance().require(NetworkService.class);
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        if (!networkService.isConnected()) {
            future.complete(false);
            return future;
        }

        JSONObject data = new JSONObject();
        data.put("invite_code", inviteCode);
        data.put("player_id", playerId.toString());
        data.put("username", username);

        networkService.sendMessage("join_group", data).thenAccept(response -> {
            if (response.optBoolean("success")) {
                Group group = parseGroup(response.getJSONObject("group"));
                cachedGroups.put(group.getId(), group);
                updatePlayerGroups(playerId, group);
                future.complete(true);
            } else {
                future.complete(false);
            }
        });

        return future;
    }

    @Override
    public CompletableFuture<Boolean> leaveGroup(String groupId, UUID playerId) {
        NetworkService networkService = ServiceManager.getInstance().require(NetworkService.class);
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        if (!networkService.isConnected()) {
            future.complete(false);
            return future;
        }

        JSONObject data = new JSONObject();
        data.put("group_id", groupId);
        data.put("player_id", playerId.toString());

        networkService.sendMessage("leave_group", data).thenAccept(response -> {
            if (response.optBoolean("success")) {
                removePlayerFromGroup(playerId, groupId);
                future.complete(true);
            } else {
                future.complete(false);
            }
        });

        return future;
    }

    @Override
    public CompletableFuture<List<Group>> getPlayerGroups(UUID playerId) {
        NetworkService networkService = ServiceManager.getInstance().require(NetworkService.class);
        CompletableFuture<List<Group>> future = new CompletableFuture<>();

        if (!networkService.isConnected()) {
            future.complete(getCachedPlayerGroups(playerId));
            return future;
        }

        JSONObject data = new JSONObject();
        data.put("player_id", playerId.toString());

        networkService.sendMessage("get_player_groups", data).thenAccept(response -> {
            if (response.optBoolean("success")) {
                List<Group> groups = parseGroups(response.getJSONArray("groups"));
                playerGroups.put(playerId, groups);
                for (Group group : groups) {
                    cachedGroups.put(group.getId(), group);
                }
                future.complete(groups);
            } else {
                future.complete(getCachedPlayerGroups(playerId));
            }
        });

        return future;
    }

    @Override
    public CompletableFuture<Optional<Group>> getGroup(String groupId) {
        NetworkService networkService = ServiceManager.getInstance().require(NetworkService.class);
        CompletableFuture<Optional<Group>> future = new CompletableFuture<>();

        Group cached = cachedGroups.get(groupId);
        if (cached != null) {
            future.complete(Optional.of(cached));
            return future;
        }

        if (!networkService.isConnected()) {
            future.complete(Optional.empty());
            return future;
        }

        JSONObject data = new JSONObject();
        data.put("group_id", groupId);

        networkService.sendMessage("get_group", data).thenAccept(response -> {
            if (response.optBoolean("success")) {
                Group group = parseGroup(response.getJSONObject("group"));
                cachedGroups.put(group.getId(), group);
                future.complete(Optional.of(group));
            } else {
                future.complete(Optional.empty());
            }
        });

        return future;
    }

    @Override
    public CompletableFuture<Boolean> deleteGroup(String groupId, UUID requesterId) {
        NetworkService networkService = ServiceManager.getInstance().require(NetworkService.class);
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        if (!networkService.isConnected()) {
            future.complete(false);
            return future;
        }

        JSONObject data = new JSONObject();
        data.put("group_id", groupId);
        data.put("requester_id", requesterId.toString());

        networkService.sendMessage("delete_group", data).thenAccept(response -> {
            if (response.optBoolean("success")) {
                cachedGroups.remove(groupId);
                removeGroupFromAllPlayers(groupId);
                future.complete(true);
            } else {
                future.complete(false);
            }
        });

        return future;
    }

    @Override
    public CompletableFuture<Boolean> updateMemberStatus(String groupId, UUID memberId, boolean isOnline, String currentServer, boolean isInVoice) {
        NetworkService networkService = ServiceManager.getInstance().require(NetworkService.class);

        if (!networkService.isConnected()) {
            return CompletableFuture.completedFuture(false);
        }

        return networkService.updatePlayerStatus(groupId, isOnline, currentServer, isInVoice);
    }

    @Override
    public CompletableFuture<String> generateInviteCode(String groupId, UUID requesterId) {
        NetworkService networkService = ServiceManager.getInstance().require(NetworkService.class);
        CompletableFuture<String> future = new CompletableFuture<>();

        if (!networkService.isConnected()) {
            future.completeExceptionally(new RuntimeException("Not connected to server"));
            return future;
        }

        JSONObject data = new JSONObject();
        data.put("group_id", groupId);
        data.put("requester_id", requesterId.toString());

        networkService.sendMessage("generate_invite", data).thenAccept(response -> {
            if (response.optBoolean("success")) {
                future.complete(response.getString("invite_code"));
            } else {
                future.completeExceptionally(new RuntimeException(response.optString("error", "Unknown error")));
            }
        });

        return future;
    }

    @Override
    public Optional<Group> getCachedGroup(String groupId) {
        return Optional.ofNullable(cachedGroups.get(groupId));
    }

    @Override
    public List<Group> getCachedPlayerGroups(UUID playerId) {
        return playerGroups.getOrDefault(playerId, new ArrayList<>());
    }

    private Group parseGroup(JSONObject data) {
        List<GroupMember> members = new ArrayList<>();
        JSONArray membersArray = data.optJSONArray("members");
        if (membersArray != null) {
            for (int i = 0; i < membersArray.length(); i++) {
                members.add(parseMember(membersArray.getJSONObject(i)));
            }
        }

        return new Group(
                data.getString("id"),
                data.getString("name"),
                UUID.fromString(data.getString("creator_id")),
                members,
                data.getLong("created_at"),
                data.optString("invite_code", "")
        );
    }

    private List<Group> parseGroups(JSONArray data) {
        List<Group> groups = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            groups.add(parseGroup(data.getJSONObject(i)));
        }
        return groups;
    }

    private GroupMember parseMember(JSONObject data) {
        return new GroupMember(
                UUID.fromString(data.getString("player_id")),
                data.getString("username"),
                GroupMember.MemberRole.valueOf(data.optString("role", "MEMBER")),
                data.getLong("joined_at"),
                data.optBoolean("is_online", false),
                data.optString("current_server", ""),
                data.optBoolean("is_in_voice", false)
        );
    }

    private void updatePlayerGroups(UUID playerId, Group group) {
        List<Group> groups = playerGroups.computeIfAbsent(playerId, k -> new ArrayList<>());
        groups.removeIf(g -> g.getId().equals(group.getId()));
        groups.add(group);
    }

    private void removePlayerFromGroup(UUID playerId, String groupId) {
        List<Group> groups = playerGroups.get(playerId);
        if (groups != null) {
            groups.removeIf(g -> g.getId().equals(groupId));
        }
    }

    private void removeGroupFromAllPlayers(String groupId) {
        for (List<Group> groups : playerGroups.values()) {
            groups.removeIf(g -> g.getId().equals(groupId));
        }
    }
}