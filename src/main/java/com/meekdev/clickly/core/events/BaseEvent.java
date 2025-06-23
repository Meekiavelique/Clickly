package com.meekdev.clickly.core.events;

public abstract class BaseEvent implements Event {
    private final long timestamp;
    private boolean cancelled = false;

    public BaseEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        if (isCancellable()) {
            this.cancelled = cancelled;
        }
    }
}