package com.meekdev.clickly.client;

import com.meekdev.clickly.Clickly;
import com.meekdev.clickly.core.ServiceManager;
import com.meekdev.clickly.core.services.*;
import com.meekdev.clickly.client.services.*;
import com.meekdev.clickly.client.handlers.*;
import com.meekdev.clickly.client.commands.ClicklyCommands;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

@Environment(EnvType.CLIENT)
public class ClicklyClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Clickly.LOGGER.info("Initializing Clickly client...");

        try {
            initializeServices();
            initializeEventHandlers();
            initializeHandlers();
            initializeCommands();
            initializeUI();

            Clickly.LOGGER.info("Clickly client initialized successfully");
        } catch (Exception e) {
            Clickly.LOGGER.error("Failed to initialize Clickly client", e);
        }
    }

    private void initializeServices() {
        ServiceManager sm = ServiceManager.getInstance();

        sm.register(ConfigService.class, new ConfigServiceImpl());
        sm.register(NetworkService.class, new NetworkServiceImpl());
        sm.register(GroupService.class, new GroupServiceImpl());
        sm.register(UIService.class, new UIServiceImpl());
    }

    private void initializeEventHandlers() {
        EventHandlerRegistry.registerAll();
    }

    private void initializeHandlers() {
        ClientConnectionHandler.register();
        ClientChatInterceptor.register();
        KeybindHandler.register();
    }

    private void initializeCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            ClicklyCommands.register(dispatcher);
        });
    }

    private void initializeUI() {
        UIServiceImpl uiService = (UIServiceImpl) ServiceManager.getInstance().require(UIService.class);
        uiService.initialize();
    }
}