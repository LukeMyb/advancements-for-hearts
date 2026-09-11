package com.example.advancements_for_hearts.mixin;

import com.example.advancements_for_hearts.AdvancementsForHeartsPlayer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancementTracker.class)
public abstract class PlayerAdvancementTrackerMixin {

    @Shadow
    private ServerPlayerEntity owner;

    @Shadow
    public abstract AdvancementProgress getProgress(Advancement advancement);

    @Inject(method = "grantCriterion", at = @At("RETURN"))
    public void onGrantCriterion(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        // もし新しい条件が達成された場合のみ (戻り値がtrue)
        if (cir.getReturnValue()) {
            AdvancementProgress progress = this.getProgress(advancement);
            // 進捗が完全に達成された瞬間かどうかを判定
            if (progress != null && progress.isDone()) {
                
                // 1. 除外フィルター
                // 裏側の内部進捗（ディスプレイ情報がない）は除外
                if (advancement.getDisplay() == null) {
                    return;
                }
                
                String path = advancement.getId().getPath();
                // レシピ解放は除外
                if (path.startsWith("recipes/")) {
                    return;
                }
                // ルート進捗（タブの起点）は除外
                if (path.endsWith("/root")) {
                    return;
                }
                
                // 2. 最大HPと現在HPの回復処理
                EntityAttributeInstance maxHealthAttr = this.owner.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                if (maxHealthAttr != null) {
                    double currentMaxHealth = maxHealthAttr.getBaseValue();
                    // 最大HPを 1.0D（ハート半分）増加させる
                    maxHealthAttr.setBaseValue(currentMaxHealth + 1.0D);
                    
                    // 現在HPを 1.0F（ハート半分）回復させる
                    this.owner.heal(1.0F);
                    
                    // 3. 効果音の再生
                    // レベルアップ音を小さめのボリューム (0.5f) と普通のピッチ (1.0f) で再生
                    this.owner.world.playSound(
                        null,
                        this.owner.getX(),
                        this.owner.getY(),
                        this.owner.getZ(),
                        SoundEvents.ENTITY_PLAYER_LEVELUP,
                        SoundCategory.PLAYERS,
                        0.5F,
                        1.0F
                    );
                }
            }
        }
    }
}
