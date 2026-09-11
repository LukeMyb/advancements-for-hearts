package com.example.advancements_for_hearts.mixin;

import com.example.advancements_for_hearts.AdvancementsForHeartsPlayer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "copyFrom", at = @At("TAIL"))
    public void copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        AdvancementsForHeartsPlayer newFhPlayer = (AdvancementsForHeartsPlayer) this;
        AdvancementsForHeartsPlayer oldFhPlayer = (AdvancementsForHeartsPlayer) oldPlayer;

        newFhPlayer.setPenaltyTimerTicks(oldFhPlayer.getPenaltyTimerTicks());
        newFhPlayer.setPenaltyTimerMs(oldFhPlayer.getPenaltyTimerMs());
        newFhPlayer.setLastIGT(oldFhPlayer.getLastIGT());
        newFhPlayer.setLastPenaltyPeriod(oldFhPlayer.getLastPenaltyPeriod());

        EntityAttributeInstance oldMaxHealth = oldPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        EntityAttributeInstance newMaxHealth = ((ServerPlayerEntity) (Object) this).getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);

        if (oldMaxHealth != null && newMaxHealth != null) {
            newMaxHealth.setBaseValue(oldMaxHealth.getBaseValue());
            ServerPlayerEntity newPlayer = (ServerPlayerEntity) (Object) this;
            if (newPlayer.getHealth() > newPlayer.getMaxHealth()) {
                newPlayer.setHealth(newPlayer.getMaxHealth());
            }
        }
    }
}
