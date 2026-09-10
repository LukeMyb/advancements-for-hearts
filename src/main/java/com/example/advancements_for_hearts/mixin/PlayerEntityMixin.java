package com.example.advancements_for_hearts.mixin;

import com.example.advancements_for_hearts.AdvancementsForHeartsPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements AdvancementsForHeartsPlayer {

    // 隠れHPストックの変数（上限4）
    private int hiddenHp = 0;

    // 初期化フラグ（初期値はfalse）
    private boolean hpInitialized = false;

    @Override
    public int getHiddenHp() {
        return this.hiddenHp;
    }

    @Override
    public void setHiddenHp(int hp) {
        // ストックは最小0、最大4に制限
        this.hiddenHp = Math.max(0, Math.min(4, hp));
        
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (!player.world.isClient && player instanceof net.minecraft.server.network.ServerPlayerEntity) {
            net.minecraft.network.PacketByteBuf buf = new net.minecraft.network.PacketByteBuf(io.netty.buffer.Unpooled.buffer());
            buf.writeInt(this.hiddenHp);
            net.fabricmc.fabric.api.network.ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, com.example.advancements_for_hearts.AdvancementsForHearts.SYNC_HIDDEN_HP_PACKET, buf);
        }
    }

    @Override
    public void addHiddenHp(int amount) {
        this.setHiddenHp(this.hiddenHp + amount);
    }

    // フラグのゲッターとセッター
    @Override
    public boolean isHpInitialized() { return this.hpInitialized; }

    @Override
    public void setHpInitialized(boolean initialized) { this.hpInitialized = initialized; }

    @Override
    public void removeHiddenHp(int amount) {
        this.setHiddenHp(this.hiddenHp - amount);
    }

    // ワールドを抜けても消えないようにNBTへ保存
    @Inject(method = "writeCustomDataToTag", at = @At("RETURN"))
    public void writeCustomDataToTag(CompoundTag tag, CallbackInfo ci) {
        tag.putInt("AdvancementsForHearts_HiddenHp", this.hiddenHp);
        // フラグを保存
        tag.putBoolean("AdvancementsForHearts_HpInitialized", this.hpInitialized);
    }

    // ワールドに入った時のNBT読み込み
    @Inject(method = "readCustomDataFromTag", at = @At("RETURN"))
    public void readCustomDataFromTag(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("AdvancementsForHearts_HiddenHp")) {
            this.hiddenHp = tag.getInt("AdvancementsForHearts_HiddenHp");
        }
        // フラグを読み込み
        if (tag.contains("AdvancementsForHearts_HpInitialized")) {
            this.hpInitialized = tag.getBoolean("AdvancementsForHearts_HpInitialized");
        }
    }
}