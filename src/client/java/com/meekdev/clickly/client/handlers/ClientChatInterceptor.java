package com.meekdev.clickly.client.handlers;

import com.meekdev.clickly.core.ServiceManager;
import com.meekdev.clickly.core.services.NetworkService;
import com.meekdev.clickly.core.services.ConfigService;
import com.meekdev.clickly.core.services.GroupService;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;

public class ClientChatInterceptor {

    public static void register() {
        ClientSendMessageEvents.ALLOW_CHAT.register(ClientChatInterceptor::onChatMessage);
    }

    private static boolean onChatMessage(String message) {
        try {
            ServiceManager sm = ServiceManager.getInstance();
            ConfigService configService = sm.require(ConfigService.class);
            NetworkService networkService = sm.require(NetworkService.class);
            GroupService groupService = sm.require(GroupService.class);

            String prefix = configService.getChatPrefix();

            if (!message.startsWith(prefix)) {
                return true;
            }

            if (!networkService.isConnected()) {
                showError("Not connected to Clickly server");
                return false;
            }

            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) {
                return false;
            }

            var groups = groupService.getCachedPlayerGroups(mc.player.getUuid());
            if (groups.isEmpty()) {
                showError("You are not in any groups");
                return false;
            }

            String chatMessage = message.substring(prefix.length()).trim();
            if (chatMessage.isEmpty()) {
                return false;
            }

            String groupId = groups.get(0).getId();
            networkService.sendChatMessage(groupId, chatMessage);

            return false;

        } catch (Exception e) {
            return true;
        }
    }

    private static void showError(String message) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(
                    net.minecraft.text.Text.literal("[Clickly] " + message)
                            .formatted(net.minecraft.util.Formatting.RED),
                    false
            );
        }
    }
}