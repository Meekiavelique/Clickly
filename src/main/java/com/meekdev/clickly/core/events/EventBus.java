package com.meekdev.clickly.core.events;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class EventBus {
    private static final EventBus INSTANCE = new EventBus();
    private final CopyOnWriteArrayList<EventHandler<?>> handlers = new CopyOnWriteArrayList<>();
    private final Executor executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "Clickly-EventBus");
        t.setDaemon(true);
        return t;
    });

    public static EventBus getInstance() {
        return INSTANCE;
    }

    public <T extends Event> void subscribe(Class<T> eventType, Consumer<T> handler) {
        handlers.add(new EventHandler<>(eventType, handler));
    }

    public void publish(Event event) {
        executor.execute(() -> {
            for (EventHandler<?> handler : handlers) {
                handler.handle(event);
            }
        });
    }

    private static class EventHandler<T extends Event> {
        private final Class<T> eventType;
        private final Consumer<T> handler;

        public EventHandler(Class<T> eventType, Consumer<T> handler) {
            this.eventType = eventType;
            this.handler = handler;
        }

        @SuppressWarnings("unchecked")
        public void handle(Event event) {
            if (eventType.isInstance(event)) {
                try {
                    handler.accept((T) event);
                } catch (Exception e) {
                    // Silent fail
                }
            }
        }
    }
}