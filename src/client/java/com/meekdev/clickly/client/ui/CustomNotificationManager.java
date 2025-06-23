package com.meekdev.clickly.client.ui;

import com.meekdev.clickly.core.services.UIService;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Iterator;

public class CustomNotificationManager {
    private final CopyOnWriteArrayList<ClicklyNotification> notifications = new CopyOnWriteArrayList<>();
    private static final int NOTIFICATION_WIDTH = 250;
    private static final int NOTIFICATION_HEIGHT = 40;
    private static final int NOTIFICATION_SPACING = 5;
    private static final long NOTIFICATION_DURATION = 5000;

    public void show(String title, String message, UIService.NotificationType type) {
        ClicklyNotification notification = new ClicklyNotification(title, message, type);
        notifications.add(notification);

        if (notifications.size() > 5) {
            notifications.remove(0);
        }
    }

    public void render(DrawContext context, int screenWidth, int screenHeight, float tickDelta) {
        Iterator<ClicklyNotification> iterator = notifications.iterator();
        while (iterator.hasNext()) {
            ClicklyNotification notification = iterator.next();
            if (notification.isExpired()) {
                iterator.remove();
                continue;
            }
            notification.updateAnimation(tickDelta);
        }

        int yOffset = 20;
        for (int i = 0; i < notifications.size(); i++) {
            ClicklyNotification notification = notifications.get(i);
            int x = screenWidth - NOTIFICATION_WIDTH - 20;
            int y = yOffset;

            notification.render(context, x, y);
            yOffset += NOTIFICATION_HEIGHT + NOTIFICATION_SPACING;
        }
    }

    private static class ClicklyNotification {
        private final String title;
        private final String message;
        private final UIService.NotificationType type;
        private final long createdTime;
        private float animationProgress = 0.0f;
        private float alpha = 1.0f;

        public ClicklyNotification(String title, String message, UIService.NotificationType type) {
            this.title = title;
            this.message = message;
            this.type = type;
            this.createdTime = System.currentTimeMillis();
        }

        public void updateAnimation(float tickDelta) {
            long elapsed = System.currentTimeMillis() - createdTime;
            if (elapsed < 300) {
                animationProgress = elapsed / 300.0f;
            } else if (elapsed > NOTIFICATION_DURATION - 500) {
                alpha = Math.max(0, (NOTIFICATION_DURATION - elapsed) / 500.0f);
            } else {
                animationProgress = 1.0f;
                alpha = 1.0f;
            }
        }

        public void render(DrawContext context, int x, int y) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.textRenderer == null) return;

            int backgroundColor = getBackgroundColor();
            int borderColor = getBorderColor();
            int titleColor = getTitleColor();
            int messageColor = 0xFFCCCCCC;

            int alphaInt = (int) (alpha * 255);
            backgroundColor = (backgroundColor & 0x00FFFFFF) | (alphaInt << 24);
            borderColor = (borderColor & 0x00FFFFFF) | (alphaInt << 24);
            titleColor = (titleColor & 0x00FFFFFF) | (alphaInt << 24);
            messageColor = (messageColor & 0x00FFFFFF) | (alphaInt << 24);

            int slideOffset = (int) ((1.0f - animationProgress) * NOTIFICATION_WIDTH);
            x += slideOffset;

            context.fill(x, y, x + NOTIFICATION_WIDTH, y + NOTIFICATION_HEIGHT, backgroundColor);
            context.drawBorder(x, y, NOTIFICATION_WIDTH, NOTIFICATION_HEIGHT, borderColor);

            context.drawText(mc.textRenderer, title, x + 8, y + 6, titleColor, false);
            context.drawText(mc.textRenderer, message, x + 8, y + 20, messageColor, false);

            int iconX = x + NOTIFICATION_WIDTH - 20;
            int iconY = y + 10;
            renderIcon(context, iconX, iconY);
        }

        private void renderIcon(DrawContext context, int x, int y) {
            int iconColor = getBorderColor();
            switch (type) {
                case SUCCESS -> {
                    context.fill(x + 4, y + 8, x + 6, y + 12, iconColor);
                    context.fill(x + 6, y + 10, x + 10, y + 12, iconColor);
                }
                case ERROR -> {
                    context.fill(x + 4, y + 4, x + 6, y + 14, iconColor);
                    context.fill(x + 8, y + 4, x + 10, y + 14, iconColor);
                    context.fill(x + 6, y + 6, x + 8, y + 8, iconColor);
                    context.fill(x + 6, y + 10, x + 8, y + 12, iconColor);
                }
                case WARNING -> {
                    context.fill(x + 6, y + 4, x + 8, y + 12, iconColor);
                    context.fill(x + 6, y + 14, x + 8, y + 16, iconColor);
                }
                default -> {
                    context.fill(x + 6, y + 6, x + 8, y + 12, iconColor);
                }
            }
        }

        private int getBackgroundColor() {
            return switch (type) {
                case SUCCESS -> 0xE0404040;
                case ERROR -> 0xE0404040;
                case WARNING -> 0xE0404040;
                default -> 0xE0404040;
            };
        }

        private int getBorderColor() {
            return switch (type) {
                case SUCCESS -> 0xFF4CAF50;
                case ERROR -> 0xFFF44336;
                case WARNING -> 0xFFFF9800;
                default -> 0xFFFF9800;
            };
        }

        private int getTitleColor() {
            return switch (type) {
                case SUCCESS -> 0xFF4CAF50;
                case ERROR -> 0xFFF44336;
                case WARNING -> 0xFFFF9800;
                default -> 0xFFFF9800;
            };
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - createdTime > NOTIFICATION_DURATION;
        }
    }
}