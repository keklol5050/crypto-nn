package com.crypto.analysis.main.enumerations;

import lombok.Getter;
@Getter
public enum Periods {

    FIVE_MINUTES("5m"),
    FIFTEEN_MINUTES("15m"),
    THIRTY_MINUTES("30m"),
    ONE_HOUR("1h"),
    TWO_HOURS("2h"),
    FOUR_HOURS("4h"),
    SIX_HOURS("6h"),
    TWELVE_HOURS("12h"),
    ONE_DAY("1d");

    private final String timeFrame;
    Periods(String timeFrame) {
        this.timeFrame = timeFrame;
    }
}
