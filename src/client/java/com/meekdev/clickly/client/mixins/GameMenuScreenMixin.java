package com.meekdev.clickly.client.mixins;

import com.meekdev.clickly.client.ui.screens.GroupManagementScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At("TAIL"))
    private void clickly_addButton(CallbackInfo ci) {
        ButtonWidget clicklyButton = ButtonWidget.builder(Text.literal("Groups"), button -> {
            if (this.client != null) {
                this.client.setScreen(new GroupManagementScreen(this));
            }
        }).dimensions(this.width / 2 - 102, this.height / 4 + 96 + -16, 204, 20).build();

        this.addDrawableChild(clicklyButton);
    }
}