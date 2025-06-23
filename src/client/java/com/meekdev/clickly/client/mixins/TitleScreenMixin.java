package com.meekdev.clickly.client.mixins;

import com.meekdev.clickly.core.ServiceManager;
import com.meekdev.clickly.core.services.UIService;
import com.meekdev.clickly.client.ui.screens.GroupManagementScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void clickly_addButton(CallbackInfo ci) {
        ButtonWidget clicklyButton = ButtonWidget.builder(Text.literal("Groups"), button -> {
            if (this.client != null) {
                this.client.setScreen(new GroupManagementScreen(this));
            }
        }).dimensions(this.width / 2 - 100, this.height / 4 + 48 + 72 + 12, 200, 20).build();

        this.addDrawableChild(clicklyButton);
    }
}