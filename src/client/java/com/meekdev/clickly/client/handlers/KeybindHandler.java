package com.meekdev.clickly.client.handlers;

import com.meekdev.clickly.core.ServiceManager;
import com.meekdev.clickly.core.services.UIService;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeybindHandler {
    private static KeyBinding toggleOverlayKey;
    private static KeyBinding openGroupsKey;

    public static void register() {
        toggleOverlayKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.clickly.toggle_overlay",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.clickly"
        ));

        openGroupsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.clickly.open_groups",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.clickly"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleOverlayKey.wasPressed()) {
                onToggleOverlay();
            }

            while (openGroupsKey.wasPressed()) {
                onOpenGroups();
            }
        });
    }

    private static void onToggleOverlay() {
        try {
            UIService uiService = ServiceManager.getInstance().require(UIService.class);
            boolean currentState = uiService.isOverlayVisible();
            uiService.setOverlayVisible(!currentState);

            uiService.showNotification(
                    "Overlay",
                    currentState ? "Hidden" : "Shown",
                    UIService.NotificationType.INFO
            );
        } catch (Exception e) {
            // Silent fail
        }
    }

    private static void onOpenGroups() {
        try {
            UIService uiService = ServiceManager.getInstance().require(UIService.class);
            uiService.showGroupManagementDialog("");
        } catch (Exception e) {
            // Silent fail
        }
    }
}