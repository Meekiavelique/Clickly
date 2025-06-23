package com.meekdev.clickly.client.ui.screens;

import com.meekdev.clickly.core.ServiceManager;
import com.meekdev.clickly.core.services.GroupService;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class JoinGroupScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget inviteCodeField;
    private ButtonWidget joinButton;
    private ButtonWidget cancelButton;
    private String statusMessage = "";
    private boolean isError = false;

    public JoinGroupScreen(Screen parent) {
        super(Text.literal("Join Group"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        inviteCodeField = new TextFieldWidget(this.textRenderer, centerX - 100, centerY - 40, 200, 20, Text.literal("Invite Code"));
        inviteCodeField.setMaxLength(16);
        inviteCodeField.setPlaceholder(Text.literal("Enter invite code..."));
        this.addSelectableChild(inviteCodeField);
        this.setInitialFocus(inviteCodeField);

        joinButton = ButtonWidget.builder(Text.literal("Join Group"), button -> {
            joinGroup();
        }).dimensions(centerX - 100, centerY + 10, 95, 20).build();
        this.addDrawableChild(joinButton);

        cancelButton = ButtonWidget.builder(Text.literal("Cancel"), button -> {
            this.client.setScreen(parent);
        }).dimensions(centerX + 5, centerY + 10, 95, 20).build();
        this.addDrawableChild(cancelButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        context.fill(centerX - 120, centerY - 80, centerX + 120, centerY + 60, 0xE0404040);
        context.drawBorder(centerX - 120, centerY - 80, 240, 140, 0xFFFF9800);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, centerY - 65, 0xFFFF9800);

        context.drawText(this.textRenderer, "Invite Code:", centerX - 100, centerY - 55, 0xFFFFFFFF, false);
        inviteCodeField.render(context, mouseX, mouseY, delta);

        if (!statusMessage.isEmpty()) {
            int color = isError ? 0xFFF44336 : 0xFF4CAF50;
            context.drawCenteredTextWithShadow(this.textRenderer, statusMessage, centerX, centerY - 15, color);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.client.setScreen(parent);
            return true;
        }
        if (keyCode == 257 && inviteCodeField.isFocused()) {
            joinGroup();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void joinGroup() {
        String inviteCode = inviteCodeField.getText().trim().toUpperCase();

        if (inviteCode.isEmpty()) {
            statusMessage = "Invite code cannot be empty";
            isError = true;
            return;
        }

        if (inviteCode.length() < 4) {
            statusMessage = "Invalid invite code format";
            isError = true;
            return;
        }

        if (this.client.player == null) {
            statusMessage = "Player not found";
            isError = true;
            return;
        }

        joinButton.active = false;
        statusMessage = "Joining group...";
        isError = false;

        GroupService groupService = ServiceManager.getInstance().require(GroupService.class);

        groupService.joinGroup(inviteCode, this.client.player.getUuid(), this.client.player.getGameProfile().getName()).thenAccept(success -> {
            this.client.execute(() -> {
                if (success) {
                    if (this.client.player != null) {
                        this.client.player.sendMessage(
                                Text.literal("Successfully joined group!")
                                        .formatted(Formatting.GREEN),
                                false
                        );
                    }
                    this.client.setScreen(parent);
                } else {
                    statusMessage = "Failed to join group. Invalid code or group is full.";
                    isError = true;
                    joinButton.active = true;
                }
            });
        });
    }
}