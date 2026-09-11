package com.example.advancements_for_hearts;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

import net.minecraft.block.Blocks;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.world.World;

public class AdvancementsForHearts implements ModInitializer {
    public static final String MOD_ID = "advancements-for-hearts";
    public static final net.minecraft.util.Identifier SYNC_HIDDEN_HP_PACKET = new net.minecraft.util.Identifier(MOD_ID, "sync_hidden_hp");

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


    // 前回の時間を記録する変数（朝が来たかの判定用）
    private long lastTimeOfDay = -1;

    @Override
    public void onInitialize() {
        System.out.println("Advancements For Hearts initialized!");



        /*
// 朝の検知とHP減少イベント
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long timeOfDay = server.getOverworld().getTimeOfDay();

            long dayTime = timeOfDay % 24000;

            if (lastTimeOfDay != -1) {
                long lastDayTime = lastTimeOfDay % 24000;

                // 夜から朝になったか、時間スキップ（就寝など）で朝になった瞬間を検知
                boolean isNewMorning = (lastDayTime > 23000 && dayTime < 1000) ||
                        (dayTime < lastDayTime && dayTime < 1000) ||
                        (dayTime - lastDayTime > 1000 && dayTime < 2000);

                if (isNewMorning) {
                    // サーバー全体のプレイヤーを取得する処理
                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                        AdvancementsForHeartsPlayer fhPlayer = (AdvancementsForHeartsPlayer) player;
                        int hiddenHp = fhPlayer.getHiddenHp();

                        if (hiddenHp > 0) {
                            // 猶予ストックがある場合は消費のみ
                            fhPlayer.removeHiddenHp(1);
                            player.sendMessage(new net.minecraft.text.TranslatableText("message.advancements-for-hearts.morning_consumed", fhPlayer.getHiddenHp()).formatted(net.minecraft.util.Formatting.YELLOW), false);
                        } else {
                            // ストックがない場合、最大HPを1（ハート半分）減少。下限は1
                            EntityAttributeInstance maxHealthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                            if (maxHealthAttr != null) {
                                double currentMaxHealth = maxHealthAttr.getBaseValue();
                                if (currentMaxHealth > 1.0D) {
                                    maxHealthAttr.setBaseValue(Math.max(1.0D, currentMaxHealth - 1.0D));
                                    // 減った最大HPに合わせて現在HPも調整
                                    if (player.getHealth() > player.getMaxHealth()) {
                                        player.setHealth(player.getMaxHealth());
                                    }
                                    player.sendMessage(new net.minecraft.text.TranslatableText("message.advancements-for-hearts.morning_decreased").formatted(net.minecraft.util.Formatting.RED), false);
                                }
                            }
                        }
                    }
                }
            }
            lastTimeOfDay = timeOfDay;
        });
*/

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
                            maxHealthAttr.setBaseValue(newMaxHealth);
                            if (player.getHealth() > player.getMaxHealth()) {
                                player.setHealth(player.getMaxHealth());
                            }
                            if (newMaxHealth <= 0.0D) {
                                player.kill();
                            }
                        }
                    }

                }
            }
        });

        // スポナー破壊イベント
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClient && state.getBlock() == Blocks.SPAWNER) {
                EntityAttributeInstance maxHealthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                if (maxHealthAttr != null) {
                    double currentMaxHealth = maxHealthAttr.getBaseValue();
                    if (currentMaxHealth < 6.0D) {
                        // 最大HPが6（ハート3つ）未満なら回復
                        maxHealthAttr.setBaseValue(Math.min(6.0D, currentMaxHealth + 1.0D));
                        player.sendMessage(new net.minecraft.text.TranslatableText("message.advancements-for-hearts.spawner_restored").formatted(net.minecraft.util.Formatting.GREEN), false);
                    } else {
                        // 最大HPが満タンなら隠れHPストックを増やす
                        AdvancementsForHeartsPlayer fhPlayer = (AdvancementsForHeartsPlayer) player;
                        if (fhPlayer.getHiddenHp() < 4) {
                            fhPlayer.addHiddenHp(1);
                            player.sendMessage(new net.minecraft.text.TranslatableText("message.advancements-for-hearts.spawner_gained", fhPlayer.getHiddenHp()).formatted(net.minecraft.util.Formatting.AQUA), false);
                        } else {
                            player.sendMessage(new net.minecraft.text.TranslatableText("message.advancements-for-hearts.spawner_maxed").formatted(net.minecraft.util.Formatting.GRAY), false);
                        }
                    }
                }
            }
        });
    }
}