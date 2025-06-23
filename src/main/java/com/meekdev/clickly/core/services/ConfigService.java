package com.meekdev.clickly.core.services;

import java.util.List;
import java.util.Optional;

public interface ConfigService {
    void save();

    void load();

    String getServerUrl();

    void setServerUrl(String url);

    List<String> getServerUrls();

    void addServerUrl(String url);

    void removeServerUrl(String url);

    boolean isOverlayEnabled();

    void setOverlayEnabled(boolean enabled);

    OverlayPosition getOverlayPosition();

    void setOverlayPosition(OverlayPosition position);

    String getChatPrefix();

    void setChatPrefix(String prefix);

    boolean isAutoConnectEnabled();

    void setAutoConnectEnabled(boolean enabled);

    boolean areNotificationsEnabled();

    void setNotificationsEnabled(boolean enabled);

    int getMaxChatHistory();

    void setMaxChatHistory(int max);

    enum OverlayPosition {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }
}