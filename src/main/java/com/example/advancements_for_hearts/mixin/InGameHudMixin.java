package com.example.advancements_for_hearts.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {
    @Shadow
    private MinecraftClient client;

    private net.minecraft.text.Text customTimerMessage;
    private int customTimerMessageTime;

    @Inject(method = "setOverlayMessage", at = @At("HEAD"), cancellable = true)
    public void onSetOverlayMessage(net.minecraft.text.Text message, boolean tinted, CallbackInfo ci) {
        String str = message.getString();
        if (str.contains("[上限:") || str.contains("[Max:")) {
            this.customTimerMessage = message;
            this.customTimerMessageTime = 60;
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void renderCustomTimer(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (this.customTimerMessageTime > 0 && this.customTimerMessage != null) {
            long igt = com.example.advancements_for_hearts.AdvancementsForHearts.getSpeedRunIGT();
            if (igt >= 0) {
                PlayerEntity player = this.client.player;
                if (player != null) {
                    net.minecraft.entity.attribute.EntityAttributeInstance attr = player.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MAX_HEALTH);
                    if (attr != null) {
                        int maxHp = (int) attr.getBaseValue();
                        int remainingMs = (int) (30000 - (igt % 30000));
                        int remainingSeconds = (int) Math.ceil(remainingMs / 1000.0);
                        this.customTimerMessage = new net.minecraft.text.TranslatableText("message.advancements-for-hearts.action_bar_timer", maxHp, remainingSeconds).formatted(net.minecraft.util.Formatting.WHITE);
                    }
                }
            }
            
            int scaledWidth = this.client.getWindow().getScaledWidth();
            int scaledHeight = this.client.getWindow().getScaledHeight();
            
            int y = scaledHeight - 68;
            int x = (scaledWidth - this.client.textRenderer.getWidth(this.customTimerMessage)) / 2;
            
            this.client.textRenderer.drawWithShadow(matrices, this.customTimerMessage, (float)x, (float)y, 0xFFFFFF);
        }
    }
    
    @Inject(method = "tick", at = @At("TAIL"))
    public void onTick(CallbackInfo ci) {
        if (this.customTimerMessageTime > 0) {
            this.customTimerMessageTime--;
        }
    }
}
