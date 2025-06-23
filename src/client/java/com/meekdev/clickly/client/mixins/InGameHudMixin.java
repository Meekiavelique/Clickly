package com.meekdev.clickly.client.mixins;

import com.meekdev.clickly.core.ServiceManager;
import com.meekdev.clickly.core.services.UIService;
import com.meekdev.clickly.client.services.UIServiceImpl;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void clickly_renderOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        try {
            UIService uiService = ServiceManager.getInstance().get(UIService.class).orElse(null);
            if (uiService instanceof UIServiceImpl impl) {
                impl.renderOverlay(context, context.getScaledWindowWidth(), context.getScaledWindowHeight(), tickCounter.getDynamicDeltaTicks());
            }
        } catch (Exception e) {
            // Silent fail
        }
    }
}