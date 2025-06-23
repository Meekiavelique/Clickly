package com.meekdev.clickly.client.ui.screens;

import com.meekdev.clickly.core.ServiceManager;
import com.meekdev.clickly.core.services.GroupService;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GroupCreationScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget groupNameField;
    private ButtonWidget createButton;
    private ButtonWidget cancelButton;
    private String errorMessage = "";

    public GroupCreationScreen(Screen parent) {
        super(Text.literal("Create Group"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        groupNameField = new TextFieldWidget(this.textRenderer, centerX - 100, centerY - 40, 200, 20, Text.literal("Group Name"));
        groupNameField.setMaxLength(32);
        groupNameField.setPlaceholder(Text.literal("Enter group name..."));
        this.addSelectableChild(groupNameField);
        this.setInitialFocus(groupNameField);

        createButton = ButtonWidget.builder(Text.literal("Create Group"), button -> {
            createGroup();
        }).dimensions(centerX - 100, centerY + 10, 95, 20).build();
        this.addDrawableChild(createButton);

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

        context.drawText(this.textRenderer, "Group Name:", centerX - 100, centerY - 55, 0xFFFFFFFF, false);
        groupNameField.render(context, mouseX, mouseY, delta);

        if (!errorMessage.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, errorMessage, centerX, centerY - 15, 0xFFF44336);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.client.setScreen(parent);
            return true;
        }
        if (keyCode == 257 && groupNameField.isFocused()) {
            createGroup();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void createGroup() {
        String groupName = groupNameField.getText().trim();

        if (groupName.isEmpty()) {
            errorMessage = "Group name cannot be empty";
            return;
        }

        if (groupName.length() < 3) {
            errorMessage = "Group name must be at least 3 characters";
            return;
        }

        if (this.client.player == null) {
            errorMessage = "Player not found";
            return;
        }

        createButton.active = false;
        errorMessage = "Creating group...";

        GroupService groupService = ServiceManager.getInstance().require(GroupService.class);

        groupService.createGroup(groupName, this.client.player.getUuid()).thenAccept(group -> {
            this.client.execute(() -> {
                if (this.client.player != null) {
                    this.client.player.sendMessage(
                            Text.literal("Created group: " + group.getName())
                                    .formatted(Formatting.GREEN),
                            false
                    );
                }
                this.client.setScreen(parent);
            });
        }).exceptionally(throwable -> {
            this.client.execute(() -> {
                errorMessage = "Failed to create group: " + throwable.getMessage();
                createButton.active = true;
            });
            return null;
        });
    }
}