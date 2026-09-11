package com.example.advancements_for_hearts;

public interface AdvancementsForHeartsPlayer {
    int getPenaltyTimerTicks();
    int getPenaltyTimerMs();
    void setPenaltyTimerMs(int ms);
    long getLastIGT();
    long getLastPenaltyPeriod();
    void setLastPenaltyPeriod(long period);
    void setLastIGT(long igt);
    void setPenaltyTimerTicks(int ticks);
}
