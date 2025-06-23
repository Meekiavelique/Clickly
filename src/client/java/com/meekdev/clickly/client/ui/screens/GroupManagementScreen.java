package com.meekdev.clickly.client.ui.screens;

import com.meekdev.clickly.core.ServiceManager;
import com.meekdev.clickly.core.services.GroupService;
import com.meekdev.clickly.core.models.Group;
import com.meekdev.clickly.core.models.GroupMember;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class GroupManagementScreen extends Screen {
    private final Screen parent;
    private List<Group> playerGroups;
    private int selectedGroupIndex = 0;
    private int scrollOffset = 0;
    private ButtonWidget createButton;
    private ButtonWidget joinButton;
    private ButtonWidget leaveButton;
    private ButtonWidget inviteButton;
    private ButtonWidget refreshButton;

    public GroupManagementScreen(Screen parent) {
        super(Text.literal("Manage Groups"));
        this.parent = parent;
        loadPlayerGroups();
    }

    @Override
    protected void init() {
        int leftPanel = this.width / 4;
        int rightPanel = this.width * 3 / 4;
        int bottomY = this.height - 40;

        createButton = ButtonWidget.builder(Text.literal("Create"), button -> {
            this.client.setScreen(new GroupCreationScreen(this));
        }).dimensions(20, bottomY, 80, 20).build();
        this.addDrawableChild(createButton);

        joinButton = ButtonWidget.builder(Text.literal("Join"), button -> {
            this.client.setScreen(new JoinGroupScreen(this));
        }).dimensions(110, bottomY, 80, 20).build();
        this.addDrawableChild(joinButton);

        refreshButton = ButtonWidget.builder(Text.literal("Refresh"), button -> {
            loadPlayerGroups();
        }).dimensions(200, bottomY, 80, 20).build();
        this.addDrawableChild(refreshButton);

        leaveButton = ButtonWidget.builder(Text.literal("Leave Group"), button -> {
            leaveSelectedGroup();
        }).dimensions(rightPanel - 100, bottomY, 100, 20).build();
        this.addDrawableChild(leaveButton);

        inviteButton = ButtonWidget.builder(Text.literal("Get Invite"), button -> {
            generateInviteCode();
        }).dimensions(rightPanel - 210, bottomY, 100, 20).build();
        this.addDrawableChild(inviteButton);

        updateButtonStates();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        int leftPanel = this.width / 4;
        int rightPanel = this.width * 3 / 4;

        context.fill(10, 10, leftPanel - 10, this.height - 60, 0xE0404040);
        context.drawBorder(10, 10, leftPanel - 20, this.height - 70, 0xFFFF9800);

        context.fill(leftPanel + 10, 10, this.width - 10, this.height - 60, 0xE0404040);
        context.drawBorder(leftPanel + 10, 10, this.width - leftPanel - 20, this.height - 70, 0xFFFF9800);

        context.drawText(this.textRenderer, "Your Groups", 20, 20, 0xFFFF9800, false);
        context.drawText(this.textRenderer, "Group Details", leftPanel + 20, 20, 0xFFFF9800, false);

        renderGroupList(context, mouseX, mouseY);
        renderGroupDetails(context);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderGroupList(DrawContext context, int mouseX, int mouseY) {
        int leftPanel = this.width / 4;
        int startY = 40;
        int entryHeight = 25;
        int maxVisible = (this.height - 120) / entryHeight;

        if (playerGroups.isEmpty()) {
            context.drawText(this.textRenderer, "No groups", 20, startY, 0xFFAAAAAA, false);
            return;
        }

        for (int i = scrollOffset; i < Math.min(playerGroups.size(), scrollOffset + maxVisible); i++) {
            Group group = playerGroups.get(i);
            int y = startY + (i - scrollOffset) * entryHeight;

            boolean isSelected = i == selectedGroupIndex;
            boolean isHovered = mouseX >= 20 && mouseX <= leftPanel - 30 && mouseY >= y && mouseY <= y + entryHeight;

            if (isSelected) {
                context.fill(20, y, leftPanel - 30, y + entryHeight, 0x80FF9800);
            } else if (isHovered) {
                context.fill(20, y, leftPanel - 30, y + entryHeight, 0x40FF9800);
            }

            String displayName = group.getName();
            if (this.textRenderer.getWidth(displayName) > leftPanel - 60) {
                displayName = this.textRenderer.trimToWidth(displayName, leftPanel - 70) + "...";
            }

            context.drawText(this.textRenderer, displayName, 25, y + 5, 0xFFFFFFFF, false);
            context.drawText(this.textRenderer, group.getMemberCount() + " members", 25, y + 14, 0xFFCCCCCC, false);
        }
    }

    private void renderGroupDetails(DrawContext context) {
        int leftPanel = this.width / 4;
        int startX = leftPanel + 20;
        int startY = 40;

        if (playerGroups.isEmpty() || selectedGroupIndex >= playerGroups.size()) {
            context.drawText(this.textRenderer, "Select a group to view details", startX, startY, 0xFFAAAAAA, false);
            return;
        }

        Group selectedGroup = playerGroups.get(selectedGroupIndex);

        context.drawText(this.textRenderer, "Name: " + selectedGroup.getName(), startX, startY, 0xFFFFFFFF, false);
        context.drawText(this.textRenderer, "Members: " + selectedGroup.getMemberCount(), startX, startY + 15, 0xFFFFFFFF, false);
        context.drawText(this.textRenderer, "Created: " + formatDate(selectedGroup.getCreatedAt()), startX, startY + 30, 0xFFCCCCCC, false);

        int memberY = startY + 60;
        context.drawText(this.textRenderer, "Members:", startX, memberY, 0xFFFF9800, false);

        for (int i = 0; i < selectedGroup.getMembers().size() && i < 10; i++) {
            GroupMember member = selectedGroup.getMembers().get(i);
            memberY += 15;

            String status = member.isOnline() ? "Online" : "Offline";
            int statusColor = member.isOnline() ? 0xFF4CAF50 : 0xFFAAAAAA;

            context.drawText(this.textRenderer, member.getUsername(), startX + 10, memberY, 0xFFFFFFFF, false);
            context.drawText(this.textRenderer, status, startX + 150, memberY, statusColor, false);

            if (member.isOnline() && !member.getCurrentServer().isEmpty()) {
                context.drawText(this.textRenderer, member.getCurrentServer(), startX + 200, memberY, 0xFFFFDD00, false);
            }
        }

        if (selectedGroup.getMembers().size() > 10) {
            context.drawText(this.textRenderer, "... and " + (selectedGroup.getMembers().size() - 10) + " more",
                    startX + 10, memberY + 15, 0xFFAAAAAA, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int leftPanel = this.width / 4;
            int startY = 40;
            int entryHeight = 25;

            if (mouseX >= 20 && mouseX <= leftPanel - 30 && mouseY >= startY) {
                int clickedIndex = (int) ((mouseY - startY) / entryHeight) + scrollOffset;
                if (clickedIndex >= 0 && clickedIndex < playerGroups.size()) {
                    selectedGroupIndex = clickedIndex;
                    updateButtonStates();
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, playerGroups.size() - (this.height - 120) / 25);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) verticalAmount));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.client.setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void loadPlayerGroups() {
        if (this.client.player == null) return;

        GroupService groupService = ServiceManager.getInstance().require(GroupService.class);
        groupService.getPlayerGroups(this.client.player.getUuid()).thenAccept(groups -> {
            this.client.execute(() -> {
                this.playerGroups = groups;
                selectedGroupIndex = Math.min(selectedGroupIndex, Math.max(0, groups.size() - 1));
                updateButtonStates();
            });
        });
    }

    private void updateButtonStates() {
        boolean hasGroups = !playerGroups.isEmpty() && selectedGroupIndex < playerGroups.size();
        leaveButton.active = hasGroups;
        inviteButton.active = hasGroups;
    }

    private void leaveSelectedGroup() {
        if (playerGroups.isEmpty() || selectedGroupIndex >= playerGroups.size() || this.client.player == null) return;

        Group selectedGroup = playerGroups.get(selectedGroupIndex);
        GroupService groupService = ServiceManager.getInstance().require(GroupService.class);

        groupService.leaveGroup(selectedGroup.getId(), this.client.player.getUuid()).thenAccept(success -> {
            this.client.execute(() -> {
                if (success) {
                    this.client.player.sendMessage(
                            Text.literal("Left group: " + selectedGroup.getName())
                                    .formatted(Formatting.YELLOW),
                            false
                    );
                    loadPlayerGroups();
                } else {
                    this.client.player.sendMessage(
                            Text.literal("Failed to leave group")
                                    .formatted(Formatting.RED),
                            false
                    );
                }
            });
        });
    }

    private void generateInviteCode() {
        if (playerGroups.isEmpty() || selectedGroupIndex >= playerGroups.size() || this.client.player == null) return;

        Group selectedGroup = playerGroups.get(selectedGroupIndex);
        GroupService groupService = ServiceManager.getInstance().require(GroupService.class);

        groupService.generateInviteCode(selectedGroup.getId(), this.client.player.getUuid()).thenAccept(inviteCode -> {
            this.client.execute(() -> {
                this.client.player.sendMessage(
                        Text.literal("Invite code for " + selectedGroup.getName() + ": " + inviteCode)
                                .formatted(Formatting.GREEN),
                        false
                );
            });
        }).exceptionally(throwable -> {
            this.client.execute(() -> {
                this.client.player.sendMessage(
                        Text.literal("Failed to generate invite code")
                                .formatted(Formatting.RED),
                        false
                );
            });
            return null;
        });
    }

    private String formatDate(long timestamp) {
        long days = (System.currentTimeMillis() - timestamp) / (1000 * 60 * 60 * 24);
        if (days == 0) return "Today";
        if (days == 1) return "Yesterday";
        return days + " days ago";
    }
}