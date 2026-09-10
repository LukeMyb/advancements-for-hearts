package com.example.advancements_for_hearts;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;

public class AdvancementsForHeartsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("Advancements For Hearts client initialized!");

        // サーバーから送られてきたhiddenHpの同期パケットを受信
        ClientSidePacketRegistry.INSTANCE.register(AdvancementsForHearts.SYNC_HIDDEN_HP_PACKET, (packetContext, attachedData) -> {
            int hiddenHp = attachedData.readInt();
            
            // メインスレッド上でクライアントのプレイヤーデータを更新する
            packetContext.getTaskQueue().execute(() -> {
                if (MinecraftClient.getInstance().player != null) {
                    AdvancementsForHeartsPlayer fhPlayer = (AdvancementsForHeartsPlayer) MinecraftClient.getInstance().player;
                    fhPlayer.setHiddenHp(hiddenHp);
                }
            });
        });
    }
}
