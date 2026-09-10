package com.example.fragile_hearts;

// プレイヤーに隠れHPを保持させるためのインターフェース
public interface FragileHeartsPlayer {
    int getHiddenHp();
    void setHiddenHp(int hp);
    void addHiddenHp(int amount);
    void removeHiddenHp(int amount);

    // 期化完了フラグのメソッド
    boolean isHpInitialized();
    void setHpInitialized(boolean initialized);
}