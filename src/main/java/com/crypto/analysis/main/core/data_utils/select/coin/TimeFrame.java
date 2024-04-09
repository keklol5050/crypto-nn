package com.crypto.analysis.main.core.data_utils.select.coin;

import lombok.Getter;

@Getter
public enum TimeFrame {
    FIFTEEN_MINUTES("15m", 15),
    ONE_HOUR("1h", 60),
    FOUR_HOUR("4h", 240);

    private final String timeFrame;
    private final int minuteCount;

    TimeFrame(String timeFrame, int minuteCount) {
        this.timeFrame = timeFrame;
        this.minuteCount = minuteCount;
    }
}
