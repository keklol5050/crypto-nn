package com.crypto.analysis.main.core.classification;

import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;

public enum PriceMovingTypes {
    HIGH_INCREASE,
    INCREASE,
    NEUTRAL,
    DECREASE,
    HIGH_DECREASE;

    public static final int countClasses = 5;

    public static double getTimeFrameChangePercentage(TimeFrame interval) {
        return switch (interval) {
            case FIFTEEN_MINUTES -> 0.6;
            case ONE_HOUR -> 1.2;
            case FOUR_HOUR -> 2.4;
        };
    }
}
