package com.meekdev.clickly.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ServiceManager {
    private static final ServiceManager INSTANCE = new ServiceManager();
    private final Map<Class<?>, Object> services = new HashMap<>();

    public static ServiceManager getInstance() {
        return INSTANCE;
    }

    public <T> void register(Class<T> serviceClass, T implementation) {
        services.put(serviceClass, implementation);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(Class<T> serviceClass) {
        return Optional.ofNullable((T) services.get(serviceClass));
    }

    public <T> T require(Class<T> serviceClass) {
        return get(serviceClass).orElseThrow(() ->
                new IllegalStateException("Service not registered: " + serviceClass.getSimpleName()));
    }

    public void shutdown() {
        services.values().forEach(service -> {
            if (service instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) service).close();
                } catch (Exception e) {
                    // Silent fail
                }
            }
        });
        services.clear();
    }
}