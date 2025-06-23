package com.meekdev.clickly.core.services;

import com.meekdev.clickly.core.models.ChatMessage;
import com.meekdev.clickly.core.models.LocationPing;

import java.util.concurrent.CompletableFuture;

public interface NetworkService extends AutoCloseable {
    CompletableFuture<Boolean> connect(String serverUrl);

    void disconnect();

    boolean isConnected();

    CompletableFuture<Boolean> authenticate(String playerId, String username);

    CompletableFuture<Boolean> sendChatMessage(String groupId, String message);

    CompletableFuture<Boolean> sendLocationPing(LocationPing ping);

    CompletableFuture<Boolean> updatePlayerStatus(String groupId, boolean isOnline, String currentServer, boolean isInVoice);

    void setMessageHandler(MessageHandler handler);

    interface MessageHandler {
        void onChatMessage(ChatMessage message);
        void onLocationPing(LocationPing ping);
        void onGroupUpdate(String groupId);
        void onConnectionStateChanged(boolean connected);
    }
}