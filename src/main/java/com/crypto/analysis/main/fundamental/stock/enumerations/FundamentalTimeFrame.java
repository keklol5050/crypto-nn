package com.crypto.analysis.main.fundamental.stock.enumerations;

import lombok.Getter;

@Getter
public enum FundamentalTimeFrame {
    FIVE_MINUTES("5min", 5),
    FIFTEEN_MINUTES("15min", 15),
    THIRTY_MINUTES("30min", 30),
    ONE_HOUR("1h", 60),
    ONE_DAY("1day", 1440);

    private final String timeFrame;
    private final int timeCount;

    FundamentalTimeFrame(String timeFrame, int timeCount) {
        this.timeFrame = timeFrame;
        this.timeCount = timeCount;
    }
}
