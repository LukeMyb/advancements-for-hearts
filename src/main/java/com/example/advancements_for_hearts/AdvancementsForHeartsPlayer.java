package com.example.advancements_for_hearts;

// プレイヤーに隠れHPを保持させるためのインターフェース
public interface AdvancementsForHeartsPlayer {
    int getHiddenHp();
    void setHiddenHp(int hp);
    void addHiddenHp(int amount);
    void removeHiddenHp(int amount);

    int getPenaltyTimerTicks();
    int getPenaltyTimerMs();
    void setPenaltyTimerMs(int ms);
    long getLastIGT();
    long getLastPenaltyPeriod();
    void setLastPenaltyPeriod(long period);
    void setLastIGT(long igt);
    void setPenaltyTimerTicks(int ticks);

    // 期化完了フラグのメソッド
    boolean isHpInitialized();
    void setHpInitialized(boolean initialized);
}