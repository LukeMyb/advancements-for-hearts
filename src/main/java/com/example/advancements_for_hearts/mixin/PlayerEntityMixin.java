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

    private int penaltyTimerTicks = 0;
    private int penaltyTimerMs = 0;
    private long lastIGT = -1L;
    private long lastPenaltyPeriod = -1L;

    @Override
    public int getPenaltyTimerTicks() { return this.penaltyTimerTicks; }

    @Override
    public void setPenaltyTimerTicks(int ticks) { this.penaltyTimerTicks = ticks; }

    @Override
    public int getPenaltyTimerMs() { return this.penaltyTimerMs; }

    @Override
    public void setPenaltyTimerMs(int ms) { this.penaltyTimerMs = ms; }

    @Override
    public long getLastIGT() { return this.lastIGT; }

    @Override
    public void setLastIGT(long igt) { this.lastIGT = igt; }

    @Override
    public long getLastPenaltyPeriod() { return this.lastPenaltyPeriod; }

    @Override
    public void setLastPenaltyPeriod(long period) { this.lastPenaltyPeriod = period; }

    @Inject(method = "writeCustomDataToTag", at = @At("RETURN"))
    public void writeCustomDataToTag(CompoundTag tag, CallbackInfo ci) {
        tag.putInt("AdvancementsForHearts_PenaltyTimerTicks", this.penaltyTimerTicks);
        tag.putInt("AdvancementsForHearts_PenaltyTimerMs", this.penaltyTimerMs);
        tag.putLong("AdvancementsForHearts_LastPenaltyPeriod", this.lastPenaltyPeriod);
    }

    @Inject(method = "readCustomDataFromTag", at = @At("RETURN"))
    public void readCustomDataFromTag(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("AdvancementsForHearts_PenaltyTimerTicks")) {
            this.penaltyTimerTicks = tag.getInt("AdvancementsForHearts_PenaltyTimerTicks");
        }
        if (tag.contains("AdvancementsForHearts_PenaltyTimerMs")) {
            this.penaltyTimerMs = tag.getInt("AdvancementsForHearts_PenaltyTimerMs");
        } else if (this.penaltyTimerTicks > 0) {
            this.penaltyTimerMs = this.penaltyTimerTicks * 50;
        }
        if (tag.contains("AdvancementsForHearts_LastPenaltyPeriod")) {
            this.lastPenaltyPeriod = tag.getLong("AdvancementsForHearts_LastPenaltyPeriod");
        }
    }
}
