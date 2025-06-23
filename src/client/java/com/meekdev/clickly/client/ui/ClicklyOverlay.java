package com.meekdev.clickly.client.ui;

import com.meekdev.clickly.core.models.Group;
import com.meekdev.clickly.core.models.ChatMessage;
import com.meekdev.clickly.core.models.LocationPing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClicklyOverlay {
    private final List<Group> groups = new CopyOnWriteArrayList<>();
    private final List<ChatMessage> chatMessages = new CopyOnWriteArrayList<>();
    private final List<LocationPing> locationPings = new CopyOnWriteArrayList<>();
    private boolean visible = true;
    private boolean connected = false;

    private static final int PANEL_WIDTH = 200;
    private static final int PANEL_HEIGHT = 150;
    private static final int BACKGROUND_COLOR = 0xE0404040;
    private static final int BORDER_COLOR = 0xFFFF9800;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int CONNECTED_COLOR = 0xFF4CAF50;
    private static final int DISCONNECTED_COLOR = 0xFFF44336;

    public void render(DrawContext context, int screenWidth, int screenHeight, float tickDelta) {
        if (!visible) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.textRenderer == null) return;

        int x = 10;
        int y = 10;

        context.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, BACKGROUND_COLOR);
        context.drawBorder(x, y, PANEL_WIDTH, PANEL_HEIGHT, BORDER_COLOR);

        int currentY = y + 8;

        String status = connected ? "Connected" : "Disconnected";
        int statusColor = connected ? CONNECTED_COLOR : DISCONNECTED_COLOR;
        context.drawText(mc.textRenderer, "Clickly " + status, x + 8, currentY, statusColor, false);
        currentY += 15;

        context.drawText(mc.textRenderer, "Groups:", x + 8, currentY, BORDER_COLOR, false);
        currentY += 12;

        if (groups.isEmpty()) {
            context.drawText(mc.textRenderer, "  No groups", x + 8, currentY, 0xFFAAAAAA, false);
        } else {
            for (Group group : groups) {
                if (currentY > y + PANEL_HEIGHT - 20) break;
                String text = "  " + group.getName() + " (" + group.getMemberCount() + ")";
                context.drawText(mc.textRenderer, text, x + 8, currentY, TEXT_COLOR, false);
                currentY += 12;
            }
        }
    }

    public void updateGroups(List<Group> groups) {
        this.groups.clear();
        this.groups.addAll(groups);
    }

    public void addChatMessage(ChatMessage message) {
        chatMessages.add(message);
        if (chatMessages.size() > 50) {
            chatMessages.remove(0);
        }
    }

    public void addLocationPing(LocationPing ping) {
        locationPings.add(ping);
        locationPings.removeIf(LocationPing::isExpired);
    }

    public void updateConnectionStatus(boolean connected) {
        this.connected = connected;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}