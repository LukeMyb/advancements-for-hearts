package com.example.advancements_for_hearts.mixin;

import com.example.advancements_for_hearts.AdvancementsForHeartsPlayer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        AdvancementsForHeartsPlayer fhPlayer = (AdvancementsForHeartsPlayer) player;

        // ログイン時に現在のhiddenHpをクライアントへ同期
        net.minecraft.network.PacketByteBuf buf = new net.minecraft.network.PacketByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeInt(fhPlayer.getHiddenHp());
        net.fabricmc.fabric.api.network.ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, com.example.advancements_for_hearts.AdvancementsForHearts.SYNC_HIDDEN_HP_PACKET, buf);

        /*
        if (!fhPlayer.isHpInitialized()) {
                    EntityAttributeInstance maxHealthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                    if (maxHealthAttr != null) {
                        maxHealthAttr.setBaseValue(20.0D); // Changed to default 20
                        player.setHealth(20.0F);
                    }
                    fhPlayer.setHpInitialized(true);
                    player.sendMessage(new net.minecraft.text.TranslatableText("message.advancements-for-hearts.survival_begun").formatted(net.minecraft.util.Formatting.LIGHT_PURPLE), false);
                }
        */
    }
}
