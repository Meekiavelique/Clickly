package com.meekdev.clickly.core.events;

public interface Event {
    long getTimestamp();

    default boolean isCancellable() {
        return false;
    }
}