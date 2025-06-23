package com.meekdev.clickly.core.events;

import com.meekdev.clickly.core.models.ChatMessage;

public class ChatEvents {

    public static class MessageReceived extends BaseEvent {
        private final ChatMessage message;

        public MessageReceived(ChatMessage message) {
            this.message = message;
        }

        public ChatMessage getMessage() {
            return message;
        }
    }

    public static class MessageSent extends BaseEvent {
        private final ChatMessage message;

        public MessageSent(ChatMessage message) {
            this.message = message;
        }

        public ChatMessage getMessage() {
            return message;
        }

        @Override
        public boolean isCancellable() {
            return true;
        }
    }
}