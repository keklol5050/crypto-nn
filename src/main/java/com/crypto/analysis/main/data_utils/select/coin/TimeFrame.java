package com.crypto.analysis.main.data_utils.select.coin;

import lombok.Getter;

@Getter
public enum TimeFrame {
    FIVE_MINUTES("5m", 5),
    FIFTEEN_MINUTES("15m", 15),
    THIRTY_MINUTES("30m", 30),
    ONE_HOUR("1h", 60),
    TWO_HOURS("2h", 120),
    FOUR_HOUR("4h", 240),
    SIX_HOURS("6h", 360),
    EIGHT_HOURS("8h", 480),
    TWELVE_HOURS("12h", 720),
    ONE_DAY("1d", 1440),
    THREE_DAYS("3d",4320);

    private final String timeFrame;
    private final int minuteCount;
    TimeFrame(String timeFrame, int minuteCount) {
        this.timeFrame = timeFrame;
        this.minuteCount = minuteCount;
    }
}
