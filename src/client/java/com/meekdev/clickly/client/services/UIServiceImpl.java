package com.meekdev.clickly.client.services;

import com.meekdev.clickly.core.services.UIService;
import com.meekdev.clickly.core.models.Group;
import com.meekdev.clickly.core.models.ChatMessage;
import com.meekdev.clickly.core.models.LocationPing;
import com.meekdev.clickly.client.ui.ClicklyOverlay;
import com.meekdev.clickly.client.ui.CustomNotificationManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class UIServiceImpl implements UIService {
    private final List<Group> currentGroups = new CopyOnWriteArrayList<>();
    private final List<ChatMessage> chatHistory = new CopyOnWriteArrayList<>();
    private final List<LocationPing> activePings = new CopyOnWriteArrayList<>();
    private final CustomNotificationManager notificationManager = new CustomNotificationManager();
    private ClicklyOverlay overlay;
    private boolean connectionStatus = false;

    public void initialize() {
        this.overlay = new ClicklyOverlay();
    }

    @Override
    public void showNotification(String title, String message, NotificationType type) {
        notificationManager.show(title, message, type);
    }

    @Override
    public void updateGroupList(List<Group> groups) {
        currentGroups.clear();
        currentGroups.addAll(groups);
        if (overlay != null) {
            overlay.updateGroups(groups);
        }
    }

    @Override
    public void addChatMessage(ChatMessage message) {
        chatHistory.add(message);
        if (chatHistory.size() > 100) {
            chatHistory.remove(0);
        }

        if (overlay != null) {
            overlay.addChatMessage(message);
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(formatChatMessage(message), false);
        }
    }

    @Override
    public void showLocationPing(LocationPing ping) {
        activePings.add(ping);
        activePings.removeIf(LocationPing::isExpired);
        if (overlay != null) {
            overlay.addLocationPing(ping);
        }
    }

    @Override
    public void updateConnectionStatus(boolean connected) {
        this.connectionStatus = connected;
        if (overlay != null) {
            overlay.updateConnectionStatus(connected);
        }
    }

    @Override
    public void showGroupCreationDialog() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null) {
            mc.execute(() -> {
                mc.setScreen(new com.meekdev.clickly.client.ui.screens.GroupCreationScreen(mc.currentScreen));
            });
        }
    }

    @Override
    public void showJoinGroupDialog() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null) {
            mc.execute(() -> {
                mc.setScreen(new com.meekdev.clickly.client.ui.screens.JoinGroupScreen(mc.currentScreen));
            });
        }
    }

    @Override
    public void showGroupManagementDialog(String groupId) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null) {
            mc.execute(() -> {
                mc.setScreen(new com.meekdev.clickly.client.ui.screens.GroupManagementScreen(mc.currentScreen));
            });
        }
    }

    @Override
    public boolean isOverlayVisible() {
        return overlay != null && overlay.isVisible();
    }

    @Override
    public void setOverlayVisible(boolean visible) {
        if (overlay != null) {
            overlay.setVisible(visible);
        }
    }

    private Text formatChatMessage(ChatMessage message) {
        return Text.literal("[CLICKLY] ")
                .formatted(Formatting.GOLD)
                .append(Text.literal("<" + message.getSenderName() + "> ")
                        .formatted(Formatting.GRAY))
                .append(Text.literal(message.getContent())
                        .formatted(Formatting.WHITE));
    }

    public List<Group> getCurrentGroups() {
        return new java.util.ArrayList<>(currentGroups);
    }

    public List<ChatMessage> getChatHistory() {
        return new java.util.ArrayList<>(chatHistory);
    }

    public List<LocationPing> getActivePings() {
        return new java.util.ArrayList<>(activePings);
    }

    public boolean getConnectionStatus() {
        return connectionStatus;
    }

    public void renderOverlay(net.minecraft.client.gui.DrawContext context, int screenWidth, int screenHeight, float tickDelta) {
        if (overlay != null) {
            overlay.render(context, screenWidth, screenHeight, tickDelta);
        }
        notificationManager.render(context, screenWidth, screenHeight, tickDelta);
    }
}