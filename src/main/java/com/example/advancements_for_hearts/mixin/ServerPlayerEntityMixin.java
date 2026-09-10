package com.example.advancements_for_hearts.mixin;

import com.example.advancements_for_hearts.AdvancementsForHeartsPlayer;
import com.example.advancements_for_hearts.AdvancementsForHearts;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "copyFrom", at = @At("TAIL"))
    public void copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        // 新しいプレイヤー（自分自身）と古いプレイヤー
        AdvancementsForHeartsPlayer newFhPlayer = (AdvancementsForHeartsPlayer) this;
        AdvancementsForHeartsPlayer oldFhPlayer = (AdvancementsForHeartsPlayer) oldPlayer;

        // 1. カスタムデータの引き継ぎ
        newFhPlayer.setHiddenHp(oldFhPlayer.getHiddenHp());
        newFhPlayer.setHpInitialized(oldFhPlayer.isHpInitialized());

        // 2. 最大HPの引き継ぎ
        EntityAttributeInstance oldMaxHealth = oldPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        EntityAttributeInstance newMaxHealth = ((ServerPlayerEntity) (Object) this).getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);

        if (oldMaxHealth != null && newMaxHealth != null) {
            newMaxHealth.setBaseValue(oldMaxHealth.getBaseValue());
            // 最大HPが減った状態の場合、現在HPも最大HPに合わせる（もし超過していれば）
            ServerPlayerEntity newPlayer = (ServerPlayerEntity) (Object) this;
            if (newPlayer.getHealth() > newPlayer.getMaxHealth()) {
                newPlayer.setHealth(newPlayer.getMaxHealth());
            }
        }

        // 3. クライアントへ同期パケットを送信
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(newFhPlayer.getHiddenHp());
        ServerSidePacketRegistry.INSTANCE.sendToPlayer((ServerPlayerEntity) (Object) this, AdvancementsForHearts.SYNC_HIDDEN_HP_PACKET, buf);
    }
}
