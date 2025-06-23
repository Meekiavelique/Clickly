package com.meekdev.clickly.client.handlers;

import com.meekdev.clickly.core.ServiceManager;
import com.meekdev.clickly.core.services.NetworkService;
import com.meekdev.clickly.core.services.ConfigService;
import com.meekdev.clickly.core.services.GroupService;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;

public class ClientConnectionHandler {

    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            onServerJoin(client);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            onServerLeave(client);
        });
    }

    private static void onServerJoin(MinecraftClient client) {
        if (client.player == null) return;

        try {
            ServiceManager sm = ServiceManager.getInstance();
            NetworkService networkService = sm.require(NetworkService.class);
            ConfigService configService = sm.require(ConfigService.class);
            GroupService groupService = sm.require(GroupService.class);

            if (configService.isAutoConnectEnabled() && !networkService.isConnected()) {
                String serverUrl = configService.getServerUrl();

                networkService.connect(serverUrl).thenAccept(success -> {
                    if (success) {
                        String playerId = client.player.getUuidAsString();
                        String username = client.player.getGameProfile().getName();

                        networkService.authenticate(playerId, username).thenAccept(authSuccess -> {
                            if (authSuccess) {
                                groupService.getPlayerGroups(client.player.getUuid());
                            }
                        });
                    }
                });
            }

            updatePlayerStatus(true);

        } catch (Exception e) {
            // Silent fail
        }
    }

    private static void onServerLeave(MinecraftClient client) {
        updatePlayerStatus(false);
    }

    private static void updatePlayerStatus(boolean online) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            ServiceManager sm = ServiceManager.getInstance();
            NetworkService networkService = sm.get(NetworkService.class).orElse(null);
            GroupService groupService = sm.get(GroupService.class).orElse(null);

            if (networkService == null || groupService == null) return;

            String currentServer = getCurrentServerName();
            var groups = groupService.getCachedPlayerGroups(client.player.getUuid());

            for (var group : groups) {
                networkService.updatePlayerStatus(group.getId(), online, currentServer, false);
            }

        } catch (Exception e) {
            // Silent fail
        }
    }

    private static String getCurrentServerName() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getCurrentServerEntry() != null) {
            return mc.getCurrentServerEntry().address;
        } else if (mc.isIntegratedServerRunning()) {
            return "Singleplayer";
        } else {
            return "Menu";
        }
    }
}