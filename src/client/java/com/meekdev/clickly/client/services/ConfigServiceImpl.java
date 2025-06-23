package com.meekdev.clickly.client.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.meekdev.clickly.Clickly;
import com.meekdev.clickly.core.services.ConfigService;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConfigServiceImpl implements ConfigService {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path configPath;
    private ClicklyConfig config;

    public ConfigServiceImpl() {
        this.configPath = FabricLoader.getInstance().getConfigDir().resolve("clickly.json");
        this.config = new ClicklyConfig();
        load();
    }

    @Override
    public void save() {
        try {
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(config));
        } catch (IOException e) {
            Clickly.LOGGER.error("Failed to save config", e);
        }
    }

    @Override
    public void load() {
        try {
            if (Files.exists(configPath)) {
                String json = Files.readString(configPath);
                config = GSON.fromJson(json, ClicklyConfig.class);
            }
            if (config == null) {
                config = new ClicklyConfig();
            }
            if (config.serverUrls == null) {
                config.serverUrls = new ArrayList<>();
                config.serverUrls.add("ws://clickly.meekdev.com:3000");
            }
        } catch (Exception e) {
            Clickly.LOGGER.error("Failed to load config", e);
            config = new ClicklyConfig();
        }
    }

    @Override
    public String getServerUrl() {
        return config.serverUrls.isEmpty() ? "ws://clickly.meekdev.com:3000" : config.serverUrls.get(0);
    }

    @Override
    public void setServerUrl(String url) {
        config.serverUrls.clear();
        config.serverUrls.add(url);
        save();
    }

    @Override
    public List<String> getServerUrls() {
        return new ArrayList<>(config.serverUrls);
    }

    @Override
    public void addServerUrl(String url) {
        if (!config.serverUrls.contains(url)) {
            config.serverUrls.add(url);
            save();
        }
    }

    @Override
    public void removeServerUrl(String url) {
        config.serverUrls.remove(url);
        save();
    }

    @Override
    public boolean isOverlayEnabled() {
        return config.overlayEnabled;
    }

    @Override
    public void setOverlayEnabled(boolean enabled) {
        config.overlayEnabled = enabled;
        save();
    }

    @Override
    public OverlayPosition getOverlayPosition() {
        return config.overlayPosition;
    }

    @Override
    public void setOverlayPosition(OverlayPosition position) {
        config.overlayPosition = position;
        save();
    }

    @Override
    public String getChatPrefix() {
        return config.chatPrefix;
    }

    @Override
    public void setChatPrefix(String prefix) {
        config.chatPrefix = prefix;
        save();
    }

    @Override
    public boolean isAutoConnectEnabled() {
        return config.autoConnect;
    }

    @Override
    public void setAutoConnectEnabled(boolean enabled) {
        config.autoConnect = enabled;
        save();
    }

    @Override
    public boolean areNotificationsEnabled() {
        return config.notificationsEnabled;
    }

    @Override
    public void setNotificationsEnabled(boolean enabled) {
        config.notificationsEnabled = enabled;
        save();
    }

    @Override
    public int getMaxChatHistory() {
        return config.maxChatHistory;
    }

    @Override
    public void setMaxChatHistory(int max) {
        config.maxChatHistory = max;
        save();
    }

    private static class ClicklyConfig {
        public List<String> serverUrls = new ArrayList<>();
        public boolean overlayEnabled = true;
        public OverlayPosition overlayPosition = OverlayPosition.TOP_LEFT;
        public String chatPrefix = "!";
        public boolean autoConnect = true;
        public boolean notificationsEnabled = true;
        public int maxChatHistory = 100;
    }
}