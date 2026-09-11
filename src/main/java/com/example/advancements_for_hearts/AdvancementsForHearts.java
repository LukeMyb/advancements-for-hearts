package com.example.advancements_for_hearts;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;

public class AdvancementsForHearts implements ModInitializer {
    public static final String MOD_ID = "advancements-for-hearts";

    // SpeedRunIGTのタイマーをリフレクションで取得するメソッド（依存関係不要）
    public static long getSpeedRunIGT() {
        try {
            Class<?> timerClass = Class.forName("com.redlimerl.speedrunigt.timer.InGameTimer");
            Object instance = timerClass.getMethod("getInstance").invoke(null);
            if (instance != null) {
                return (long) timerClass.getMethod("getInGameTime").invoke(instance);
            }
        } catch (Exception e) {
            // SpeedRunIGTが存在しない、または取得できない場合
        }
        return -1L;
    }

    @Override
    public void onInitialize() {
        System.out.println("Advancements For Hearts initialized!");

        
        // 第1段階: 30秒(600Tick)ごとの最大HP減少とキル処理
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                // サバイバルモード・アドベンチャーモードなど、通常プレイ中のみタイマーを進めるのが理想だが、
                // ひとまず全プレイヤーに対してTickを進める
                if (player.isAlive() && !player.isCreative() && !player.isSpectator()) {
                    AdvancementsForHeartsPlayer fhPlayer = (AdvancementsForHeartsPlayer) player;
                    long currentIGT = getSpeedRunIGT();
                    int remainingMs = 0;
                    boolean applyPenalty = false;

                    if (currentIGT >= 0) {
                        // SpeedRunIGTが存在する場合、絶対的なIGT時間から周期を計算
                        long currentPeriod = currentIGT / 30000;
                        long lastPeriod = fhPlayer.getLastPenaltyPeriod();
                        
                        if (lastPeriod == -1L) {
                            // 初期化
                            fhPlayer.setLastPenaltyPeriod(currentPeriod);
                        } else if (currentPeriod > lastPeriod) {
                            applyPenalty = true;
                            fhPlayer.setLastPenaltyPeriod(currentPeriod);
                        }
                        
                        remainingMs = (int) (30000 - (currentIGT % 30000));
                        
                        // 視覚的なズレ（ラグ）を無くすため、毎Tickアクションバーへ送信し、表示を常に最新のIGTと同期させる
                        int remainingSeconds = (int) Math.ceil(remainingMs / 1000.0);
                        EntityAttributeInstance attr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                        if (attr != null) {
                            int maxHp = (int) attr.getBaseValue();
                            player.sendMessage(new net.minecraft.text.TranslatableText("message.advancements-for-hearts.action_bar_timer", maxHp, remainingSeconds).formatted(net.minecraft.util.Formatting.WHITE), true);
                        }
                    } else {
                        // SpeedRunIGTが存在しない場合のフォールバック（Tickベース）
                        int currentMs = fhPlayer.getPenaltyTimerMs() + 50;
                        if (currentMs >= 30000) {
                            currentMs -= 30000;
                            applyPenalty = true;
                        }
                        fhPlayer.setPenaltyTimerMs(currentMs);
                        remainingMs = 30000 - currentMs;
                        
                        // こちらはTickベースなので20Tickに1回の送信で十分
                        int ticks = fhPlayer.getPenaltyTimerTicks();
                        ticks++;
                        if (ticks >= 20) {
                            ticks = 0;
                            int remainingSeconds = (int) Math.ceil(remainingMs / 1000.0);
                            EntityAttributeInstance attr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                            if (attr != null) {
                                int maxHp = (int) attr.getBaseValue();
                                player.sendMessage(new net.minecraft.text.TranslatableText("message.advancements-for-hearts.action_bar_timer", maxHp, remainingSeconds).formatted(net.minecraft.util.Formatting.WHITE), true);
                            }
                        }
                        fhPlayer.setPenaltyTimerTicks(ticks);
                    }
                    
                    if (applyPenalty) {
                        EntityAttributeInstance maxHealthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                        if (maxHealthAttr != null) {
                            double currentMaxHealth = maxHealthAttr.getBaseValue();
                            double newMaxHealth = currentMaxHealth - 1.0D;
                            // 0未満にはならないように制限する
                            if (newMaxHealth < 0.0D) {
                                newMaxHealth = 0.0D;
                            }
                            maxHealthAttr.setBaseValue(newMaxHealth);
                            if (player.getHealth() > player.getMaxHealth()) {
                                player.setHealth(player.getMaxHealth());
                            }
                            if (newMaxHealth <= 0.0D) {
                                player.kill();
                            }
                        }
                    }
                    
                    // 常に最大HPを監視し、0以下の場合はリスポーン直後でも即座にキルする
                    EntityAttributeInstance maxHealthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                    if (maxHealthAttr != null && maxHealthAttr.getBaseValue() <= 0.0D) {
                        player.kill();
                    }

                }
            }
        });

    }
}