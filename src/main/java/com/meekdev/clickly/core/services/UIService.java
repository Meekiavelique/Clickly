package com.meekdev.clickly.core.services;

import com.meekdev.clickly.core.models.Group;
import com.meekdev.clickly.core.models.ChatMessage;
import com.meekdev.clickly.core.models.LocationPing;

import java.util.List;

public interface UIService {
    void showNotification(String title, String message, NotificationType type);

    void updateGroupList(List<Group> groups);

    void addChatMessage(ChatMessage message);

    void showLocationPing(LocationPing ping);

    void updateConnectionStatus(boolean connected);

    void showGroupCreationDialog();

    void showJoinGroupDialog();

    void showGroupManagementDialog(String groupId);

    boolean isOverlayVisible();

    void setOverlayVisible(boolean visible);

    enum NotificationType {
        INFO,
        SUCCESS,
        WARNING,
        ERROR
    }
}