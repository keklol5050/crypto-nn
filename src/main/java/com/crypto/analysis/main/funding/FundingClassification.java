package com.crypto.analysis.main.funding;

public enum FundingClassification {
    GROWTH,
    INCREASE_HIGH,
    INCREASE_MEDIUM,
    INCREASE_LOW,
    ZERO,
    DECREASE_LOW, // 5
    DECREASE_MEDIUM,
    DECREASE_HIGH,
    FALL,
    SPECULATIVE_PRICE_INCREASE_VOLUME_DECREASE_HIGH_RATE,
    SPECULATIVE_PRICE_INCREASE_VOLUME_DECREASE_LOW_RATE, // 10
    SPECULATIVE_PRICE_DECREASE_VOLUME_DECREASE_LOW_RATE,
    SPECULATIVE_PRICE_DECREASE_VOLUME_DECREASE_HIGH_RATE,
    PRICE_INCREASE_VOLUME_INCREASE_HIGH,
    PRICE_INCREASE_VOLUME_INCREASE_LOW,
    PRICE_DECREASE_VOLUME_INCREASE_LOW, // 13
    VOLUME_FALL_PRICE_NOT_CHANGED,
    VOLUME_GROWTH_PRICE_NOT_CHANGED,
    VOLUME_FALL_WHEN_PRICE_INCREASE,
    VOLUME_FALL_WHEN_PRICE_DECREASE,
    FALL_VOLUME_DECREASE;

    public static FundingClassification getClassification(double changePrice, double changeVolume) {
        if (Math.abs(changePrice) < 200 && Math.abs(changeVolume) < 200) return ZERO;
        if (Math.abs(changePrice) < 200 && Math.abs(changeVolume) > 7000) {
            if (changeVolume < 0) return VOLUME_FALL_PRICE_NOT_CHANGED;
            else return VOLUME_GROWTH_PRICE_NOT_CHANGED;
        }
        if (changePrice > 0) {
            if (changePrice > 1000 && changeVolume > 20000) return GROWTH;
            else if (changePrice > 1000 && changeVolume > 10000) return INCREASE_HIGH;
            else if (changePrice > 600 && changeVolume > 8000) return INCREASE_MEDIUM;
            else if (changeVolume < -10000 && !(changePrice>600)) return VOLUME_FALL_WHEN_PRICE_INCREASE;
            else if ((changePrice > 600 && changeVolume < 0)) return SPECULATIVE_PRICE_INCREASE_VOLUME_DECREASE_HIGH_RATE;
            else if (changeVolume > 10000) return PRICE_INCREASE_VOLUME_INCREASE_HIGH;
            else if (changeVolume < 0) return SPECULATIVE_PRICE_INCREASE_VOLUME_DECREASE_LOW_RATE;
            else if (changeVolume > 8000) return PRICE_INCREASE_VOLUME_INCREASE_LOW;
            else return INCREASE_LOW;
        }
        if (changePrice <= 0) {
            if (changePrice < -1000 && changeVolume > 20000) return FALL;
            else if (changePrice < -1000 && changeVolume > 10000) return DECREASE_HIGH;
            else if (changePrice < -600 && changeVolume > 8000) return DECREASE_MEDIUM;
            else if (changePrice < -600 && changeVolume < -10000) return FALL_VOLUME_DECREASE;
            else if (changeVolume < -10000) return VOLUME_FALL_WHEN_PRICE_DECREASE;
            else if (changePrice < -600 && changeVolume < 0) return SPECULATIVE_PRICE_DECREASE_VOLUME_DECREASE_HIGH_RATE;
            else if (changeVolume < 0) return SPECULATIVE_PRICE_DECREASE_VOLUME_DECREASE_LOW_RATE;
            else if (changeVolume > 8000) return PRICE_DECREASE_VOLUME_INCREASE_LOW;
            else return DECREASE_LOW;
        }
        return null;
    }
}
