package com.crypto.analysis.main.data_utils.select.coin;

import lombok.Getter;

@Getter
public enum TimeFrame {
    FIVE_MINUTES("5m"),
    FIFTEEN_MINUTES("15m"),
    THIRTY_MINUTES("30m"),
    ONE_HOUR("1h"),
    TWO_HOURS("2h"),
    FOUR_HOURS("4h"),
    SIX_HOURS("6h"),
    EIGHT_HOURS("8h"),
    TWELVE_HOURS("12h"),
    ONE_DAY("1d"),
    THREE_DAYS("3d");

    private final String timeFrame;
    TimeFrame(String timeFrame) {
        this.timeFrame = timeFrame;
    }
}
