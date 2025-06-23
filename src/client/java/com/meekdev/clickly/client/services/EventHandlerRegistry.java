package com.meekdev.clickly.client.services;

import com.meekdev.clickly.core.ServiceManager;
import com.meekdev.clickly.core.events.EventBus;
import com.meekdev.clickly.core.events.ChatEvents;
import com.meekdev.clickly.core.events.NetworkEvents;
import com.meekdev.clickly.core.events.GroupEvents;
import com.meekdev.clickly.core.services.UIService;
import com.meekdev.clickly.core.services.GroupService;

public class EventHandlerRegistry {

    public static void registerAll() {
        EventBus eventBus = EventBus.getInstance();
        ServiceManager serviceManager = ServiceManager.getInstance();

        eventBus.subscribe(ChatEvents.MessageReceived.class, event -> {
            UIService uiService = serviceManager.require(UIService.class);
            uiService.addChatMessage(event.getMessage());
        });

        eventBus.subscribe(NetworkEvents.LocationPingReceived.class, event -> {
            UIService uiService = serviceManager.require(UIService.class);
            uiService.showLocationPing(event.getPing());
        });

        eventBus.subscribe(NetworkEvents.Connected.class, event -> {
            UIService uiService = serviceManager.require(UIService.class);
            uiService.updateConnectionStatus(true);
            uiService.showNotification("Connected", "Connected to " + event.getServerUrl(), UIService.NotificationType.SUCCESS);
        });

        eventBus.subscribe(NetworkEvents.Disconnected.class, event -> {
            UIService uiService = serviceManager.require(UIService.class);
            uiService.updateConnectionStatus(false);
            uiService.showNotification("Disconnected", event.getReason(), UIService.NotificationType.WARNING);
        });

        eventBus.subscribe(NetworkEvents.ConnectionFailed.class, event -> {
            UIService uiService = serviceManager.require(UIService.class);
            uiService.showNotification("Connection Failed", event.getError(), UIService.NotificationType.ERROR);
        });

        eventBus.subscribe(GroupEvents.GroupCreated.class, event -> {
            UIService uiService = serviceManager.require(UIService.class);
            uiService.showNotification("Group Created", "Successfully created " + event.getGroup().getName(), UIService.NotificationType.SUCCESS);
        });

        eventBus.subscribe(GroupEvents.GroupJoined.class, event -> {
            UIService uiService = serviceManager.require(UIService.class);
            GroupService groupService = serviceManager.require(GroupService.class);
            uiService.showNotification("Joined Group", event.getMember().getUsername() + " joined the group", UIService.NotificationType.INFO);
        });

        eventBus.subscribe(GroupEvents.GroupLeft.class, event -> {
            UIService uiService = serviceManager.require(UIService.class);
            uiService.showNotification("Left Group", event.getMember().getUsername() + " left the group", UIService.NotificationType.INFO);
        });
    }
}