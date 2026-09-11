package com.example.advancements_for_hearts.mixin;

import com.example.advancements_for_hearts.AdvancementsForHeartsPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {

    @Shadow
    private MinecraftClient client;

    // 3つのテクスチャをそれぞれ定義
    private static final Identifier EMPTY_HEART = new Identifier("advancements-for-hearts", "textures/gui/empty_heart.png");
    private static final Identifier HALF_HEART = new Identifier("advancements-for-hearts", "textures/gui/half_heart.png");
    private static final Identifier FULL_HEART = new Identifier("advancements-for-hearts", "textures/gui/full_heart.png");

    @Inject(method = "renderStatusBars", at = @At("TAIL"))
    private void renderHiddenHearts(MatrixStack matrices, CallbackInfo ci) {
/*
        PlayerEntity player = this.client.player;
        if (player == null) return;

        AdvancementsForHeartsPlayer fhPlayer = (AdvancementsForHeartsPlayer) player;
        int hiddenHp = fhPlayer.getHiddenHp();

        int scaledWidth = this.client.getWindow().getScaledWidth();
        int scaledHeight = this.client.getWindow().getScaledHeight();

        // バニラの体力のベース位置（Xは中央から左に91ピクセル、Yは下から39ピクセル）
        int baseX = scaledWidth / 2 - 91;
        int baseY = scaledHeight - 39;

        // 現在の最大HPから、バニラハートが何個描画されているかを計算
        float maxHealth = player.getMaxHealth();

        // 金のリンゴ等による衝撃吸収（Absorption）の量を取得
        float absorption = player.getAbsorptionAmount();

        // 赤ハートと金ハートの数を合計し、動的な開始位置を算出する
        int redHeartCount = (int) Math.ceil(maxHealth / 2.0f);
        int goldenHeartCount = (int) Math.ceil(absorption / 2.0f);
        int vanillaHeartCount = redHeartCount + goldenHeartCount;

        // RenderSystemの設定（透過とカラーのリセット）
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        com.mojang.blaze3d.systems.RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        // 常に2個の枠を描画し、中身を重ねる処理（最大ストック4 = ハート2個分）
        for (int i = 0; i < 2; i++) {
            // このハートが全体で何個目か（0番目スタート）
            int index = vanillaHeartCount + i;

            // 10個で折り返すX座標の計算（余りを利用）
            int x = baseX + (index % 10) * 8;

            // 10個を超えるごとに上に10ピクセルずらすY座標の計算（商を利用）
            int y = baseY - (index / 10) * 10;

            // まず背景の「空の枠」を描画
            this.client.getTextureManager().bindTexture(EMPTY_HEART);
            DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 9, 9, 9, 9);

            // この枠に入るべきストック数を計算（0, 1, 2のいずれか）
            int hpInThisHeart = Math.max(0, Math.min(2, hiddenHp - (i * 2)));

            // 中身を重ねて描画
            if (hpInThisHeart == 2) {
                this.client.getTextureManager().bindTexture(FULL_HEART);
                DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 9, 9, 9, 9);
            } else if (hpInThisHeart == 1) {
                this.client.getTextureManager().bindTexture(HALF_HEART);
                DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 9, 9, 9, 9);
            }
        }
        
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        */
    }
}