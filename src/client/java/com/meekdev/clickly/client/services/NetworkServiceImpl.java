package com.meekdev.clickly.client.services;

import com.meekdev.clickly.Clickly;
import com.meekdev.clickly.core.services.NetworkService;
import com.meekdev.clickly.core.models.ChatMessage;
import com.meekdev.clickly.core.models.LocationPing;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONObject;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class NetworkServiceImpl implements NetworkService {
    private final AtomicReference<Socket> socket = new AtomicReference<>();
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicReference<MessageHandler> messageHandler = new AtomicReference<>();
    private String currentPlayerId;
    private String currentUsername;

    @Override
    public CompletableFuture<Boolean> connect(String serverUrl) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        try {
            IO.Options options = IO.Options.builder()
                    .setReconnection(true)
                    .setReconnectionAttempts(5)
                    .setReconnectionDelay(2000)
                    .setTimeout(10000)
                    .build();

            Socket newSocket = IO.socket(URI.create(serverUrl), options);

            newSocket.on(Socket.EVENT_CONNECT, args -> {
                connected.set(true);
                notifyConnectionState(true);
                future.complete(true);
            });

            newSocket.on(Socket.EVENT_DISCONNECT, args -> {
                connected.set(false);
                notifyConnectionState(false);
            });

            newSocket.on(Socket.EVENT_CONNECT_ERROR, args -> {
                connected.set(false);
                notifyConnectionState(false);
                if (!future.isDone()) {
                    future.complete(false);
                }
            });

            newSocket.on("chat_message", this::handleChatMessage);
            newSocket.on("location_ping", this::handleLocationPing);
            newSocket.on("group_update", this::handleGroupUpdate);

            socket.set(newSocket);
            newSocket.connect();

        } catch (Exception e) {
            Clickly.LOGGER.error("Failed to connect to server", e);
            future.complete(false);
        }

        return future;
    }

    @Override
    public void disconnect() {
        Socket currentSocket = socket.get();
        if (currentSocket != null) {
            currentSocket.disconnect();
            connected.set(false);
            notifyConnectionState(false);
        }
    }

    @Override
    public boolean isConnected() {
        return connected.get();
    }

    @Override
    public CompletableFuture<Boolean> authenticate(String playerId, String username) {
        this.currentPlayerId = playerId;
        this.currentUsername = username;

        Socket currentSocket = socket.get();
        if (currentSocket == null || !connected.get()) {
            return CompletableFuture.completedFuture(false);
        }

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        JSONObject authData = new JSONObject();
        authData.put("player_id", playerId);
        authData.put("username", username);

        currentSocket.once("auth_response", args -> {
            if (args.length > 0 && args[0] instanceof JSONObject response) {
                boolean success = response.optBoolean("success", false);
                future.complete(success);
            } else {
                future.complete(false);
            }
        });

        currentSocket.emit("authenticate", authData);
        return future;
    }

    @Override
    public CompletableFuture<Boolean> sendChatMessage(String groupId, String message) {
        Socket currentSocket = socket.get();
        if (currentSocket == null || !connected.get()) {
            return CompletableFuture.completedFuture(false);
        }

        JSONObject data = new JSONObject();
        data.put("group_id", groupId);
        data.put("message", message);

        currentSocket.emit("send_chat", data);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> sendLocationPing(LocationPing ping) {
        Socket currentSocket = socket.get();
        if (currentSocket == null || !connected.get()) {
            return CompletableFuture.completedFuture(false);
        }

        JSONObject data = new JSONObject();
        data.put("group_id", ping.getGroupId());
        data.put("x", ping.getX());
        data.put("y", ping.getY());
        data.put("z", ping.getZ());
        data.put("dimension", ping.getDimension());
        data.put("server", ping.getServer());

        currentSocket.emit("send_ping", data);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> updatePlayerStatus(String groupId, boolean isOnline, String currentServer, boolean isInVoice) {
        Socket currentSocket = socket.get();
        if (currentSocket == null || !connected.get()) {
            return CompletableFuture.completedFuture(false);
        }

        JSONObject data = new JSONObject();
        data.put("group_id", groupId);
        data.put("is_online", isOnline);
        data.put("current_server", currentServer);
        data.put("is_in_voice", isInVoice);

        currentSocket.emit("update_status", data);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public void setMessageHandler(MessageHandler handler) {
        messageHandler.set(handler);
    }

    private void handleChatMessage(Object... args) {
        if (args.length > 0 && args[0] instanceof JSONObject data) {
            try {
                ChatMessage message = new ChatMessage(
                        UUID.fromString(data.getString("sender_id")),
                        data.getString("sender_name"),
                        data.getString("content"),
                        data.getLong("timestamp"),
                        data.getString("group_id"),
                        ChatMessage.MessageType.valueOf(data.optString("type", "NORMAL"))
                );

                MessageHandler handler = messageHandler.get();
                if (handler != null) {
                    handler.onChatMessage(message);
                }
            } catch (Exception e) {
                Clickly.LOGGER.error("Error parsing chat message", e);
            }
        }
    }

    private void handleLocationPing(Object... args) {
        if (args.length > 0 && args[0] instanceof JSONObject data) {
            try {
                LocationPing ping = new LocationPing(
                        UUID.fromString(data.getString("sender_id")),
                        data.getString("sender_name"),
                        data.getInt("x"),
                        data.getInt("y"),
                        data.getInt("z"),
                        data.getString("dimension"),
                        data.getString("server"),
                        data.getLong("timestamp"),
                        data.getString("group_id")
                );

                MessageHandler handler = messageHandler.get();
                if (handler != null) {
                    handler.onLocationPing(ping);
                }
            } catch (Exception e) {
                Clickly.LOGGER.error("Error parsing location ping", e);
            }
        }
    }

    private void handleGroupUpdate(Object... args) {
        if (args.length > 0 && args[0] instanceof JSONObject data) {
            String groupId = data.optString("group_id");
            if (!groupId.isEmpty()) {
                MessageHandler handler = messageHandler.get();
                if (handler != null) {
                    handler.onGroupUpdate(groupId);
                }
            }
        }
    }

    private void notifyConnectionState(boolean connected) {
        MessageHandler handler = messageHandler.get();
        if (handler != null) {
            handler.onConnectionStateChanged(connected);
        }
    }

    @Override
    public void close() {
        disconnect();
    }
}