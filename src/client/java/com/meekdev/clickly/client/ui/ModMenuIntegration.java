package com.meekdev.clickly.client.ui;

import com.meekdev.clickly.core.ServiceManager;
import com.meekdev.clickly.core.services.ConfigService;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ClicklyConfigScreen::new;
    }

    public static class ClicklyConfigScreen extends Screen {
        private final Screen parent;
        private ConfigService configService;
        private TextFieldWidget serverUrlField;
        private TextFieldWidget chatPrefixField;
        private ButtonWidget overlayToggleButton;
        private ButtonWidget autoConnectToggleButton;
        private ButtonWidget notificationsToggleButton;

        public ClicklyConfigScreen(Screen parent) {
            super(Text.literal("Clickly Configuration"));
            this.parent = parent;
            this.configService = ServiceManager.getInstance().require(ConfigService.class);
        }

        @Override
        protected void init() {
            int centerX = this.width / 2;
            int startY = 60;
            int spacing = 30;

            Text serverUrlLabel = Text.literal("Server URL:");
            this.addDrawableChild(ButtonWidget.builder(Text.literal("← Back"), button -> {
                this.client.setScreen(parent);
            }).dimensions(20, 20, 60, 20).build());

            serverUrlField = new TextFieldWidget(this.textRenderer, centerX - 100, startY, 200, 20, Text.literal("Server URL"));
            serverUrlField.setText(configService.getServerUrl());
            this.addSelectableChild(serverUrlField);

            chatPrefixField = new TextFieldWidget(this.textRenderer, centerX - 100, startY + spacing, 200, 20, Text.literal("Chat Prefix"));
            chatPrefixField.setText(configService.getChatPrefix());
            chatPrefixField.setMaxLength(5);
            this.addSelectableChild(chatPrefixField);

            overlayToggleButton = ButtonWidget.builder(
                    Text.literal("Overlay: " + (configService.isOverlayEnabled() ? "ON" : "OFF")),
                    button -> {
                        boolean newState = !configService.isOverlayEnabled();
                        configService.setOverlayEnabled(newState);
                        button.setMessage(Text.literal("Overlay: " + (newState ? "ON" : "OFF")));
                    }
            ).dimensions(centerX - 100, startY + spacing * 2, 200, 20).build();
            this.addDrawableChild(overlayToggleButton);

            autoConnectToggleButton = ButtonWidget.builder(
                    Text.literal("Auto Connect: " + (configService.isAutoConnectEnabled() ? "ON" : "OFF")),
                    button -> {
                        boolean newState = !configService.isAutoConnectEnabled();
                        configService.setAutoConnectEnabled(newState);
                        button.setMessage(Text.literal("Auto Connect: " + (newState ? "ON" : "OFF")));
                    }
            ).dimensions(centerX - 100, startY + spacing * 3, 200, 20).build();
            this.addDrawableChild(autoConnectToggleButton);

            notificationsToggleButton = ButtonWidget.builder(
                    Text.literal("Notifications: " + (configService.areNotificationsEnabled() ? "ON" : "OFF")),
                    button -> {
                        boolean newState = !configService.areNotificationsEnabled();
                        configService.setNotificationsEnabled(newState);
                        button.setMessage(Text.literal("Notifications: " + (newState ? "ON" : "OFF")));
                    }
            ).dimensions(centerX - 100, startY + spacing * 4, 200, 20).build();
            this.addDrawableChild(notificationsToggleButton);

            this.addDrawableChild(ButtonWidget.builder(Text.literal("Save"), button -> {
                saveConfig();
                this.client.setScreen(parent);
            }).dimensions(centerX - 50, startY + spacing * 6, 100, 20).build());
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            this.renderBackground(context, mouseX, mouseY, delta);

            context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 30, 0xFFFF9800);

            int centerX = this.width / 2;
            int startY = 60;
            int spacing = 30;

            context.drawText(this.textRenderer, "Server URL:", centerX - 100, startY - 12, 0xFFFFFFFF, false);
            serverUrlField.render(context, mouseX, mouseY, delta);

            context.drawText(this.textRenderer, "Chat Prefix:", centerX - 100, startY + spacing - 12, 0xFFFFFFFF, false);
            chatPrefixField.render(context, mouseX, mouseY, delta);

            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == 256) {
                this.client.setScreen(parent);
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        private void saveConfig() {
            String serverUrl = serverUrlField.getText().trim();
            String chatPrefix = chatPrefixField.getText().trim();

            if (!serverUrl.isEmpty()) {
                configService.setServerUrl(serverUrl);
            }

            if (!chatPrefix.isEmpty()) {
                configService.setChatPrefix(chatPrefix);
            }

            configService.save();
        }
    }
}