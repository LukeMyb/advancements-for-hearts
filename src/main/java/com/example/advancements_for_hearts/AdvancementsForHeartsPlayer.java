package com.example.advancements_for_hearts;

// プレイヤーに隠れHPを保持させるためのインターフェース
public interface AdvancementsForHeartsPlayer {
    int getHiddenHp();
    void setHiddenHp(int hp);
    void addHiddenHp(int amount);
    void removeHiddenHp(int amount);

    int getPenaltyTimerTicks();
    void setPenaltyTimerTicks(int ticks);

    // 期化完了フラグのメソッド
    boolean isHpInitialized();
    void setHpInitialized(boolean initialized);
}