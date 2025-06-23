package com.meekdev.clickly.core.events;

import com.meekdev.clickly.core.models.LocationPing;

public class NetworkEvents {

    public static class Connected extends BaseEvent {
        private final String serverUrl;

        public Connected(String serverUrl) {
            this.serverUrl = serverUrl;
        }

        public String getServerUrl() {
            return serverUrl;
        }
    }

    public static class Disconnected extends BaseEvent {
        private final String reason;

        public Disconnected(String reason) {
            this.reason = reason;
        }

        public String getReason() {
            return reason;
        }
    }

    public static class ConnectionFailed extends BaseEvent {
        private final String serverUrl;
        private final String error;

        public ConnectionFailed(String serverUrl, String error) {
            this.serverUrl = serverUrl;
            this.error = error;
        }

        public String getServerUrl() {
            return serverUrl;
        }

        public String getError() {
            return error;
        }
    }

    public static class LocationPingReceived extends BaseEvent {
        private final LocationPing ping;

        public LocationPingReceived(LocationPing ping) {
            this.ping = ping;
        }

        public LocationPing getPing() {
            return ping;
        }
    }

    public static class LocationPingSent extends BaseEvent {
        private final LocationPing ping;

        public LocationPingSent(LocationPing ping) {
            this.ping = ping;
        }

        public LocationPing getPing() {
            return ping;
        }

        @Override
        public boolean isCancellable() {
            return true;
        }
    }
}