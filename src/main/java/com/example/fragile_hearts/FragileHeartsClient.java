package com.example.fragile_hearts;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;

public class FragileHeartsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("Fragile Hearts client initialized!");

        // サーバーから送られてきたhiddenHpの同期パケットを受信
        ClientSidePacketRegistry.INSTANCE.register(FragileHearts.SYNC_HIDDEN_HP_PACKET, (packetContext, attachedData) -> {
            int hiddenHp = attachedData.readInt();
            
            // メインスレッド上でクライアントのプレイヤーデータを更新する
            packetContext.getTaskQueue().execute(() -> {
                if (MinecraftClient.getInstance().player != null) {
                    FragileHeartsPlayer fhPlayer = (FragileHeartsPlayer) MinecraftClient.getInstance().player;
                    fhPlayer.setHiddenHp(hiddenHp);
                }
            });
        });
    }
}
