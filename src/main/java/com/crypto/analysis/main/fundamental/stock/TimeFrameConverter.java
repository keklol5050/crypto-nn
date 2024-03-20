package com.crypto.analysis.main.fundamental.stock;

import com.crypto.analysis.main.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.data_utils.select.fundamental.FundamentalTimeFrame;

public class TimeFrameConverter {
    public static TimeFrame convert(FundamentalTimeFrame fundamentalTimeFrame) {
        return switch (fundamentalTimeFrame) {
            case FIVE_MINUTES -> TimeFrame.FIVE_MINUTES;
            case FIFTEEN_MINUTES -> TimeFrame.FIFTEEN_MINUTES;
            case THIRTY_MINUTES -> TimeFrame.THIRTY_MINUTES;
            case ONE_HOUR -> TimeFrame.ONE_HOUR;
            case ONE_DAY -> TimeFrame.ONE_DAY;
            default -> throw new IllegalArgumentException();
        };
    }

    public static FundamentalTimeFrame convert(TimeFrame timeFrame) {
        return switch (timeFrame) {
            case FIVE_MINUTES -> FundamentalTimeFrame.FIVE_MINUTES;
            case FIFTEEN_MINUTES -> FundamentalTimeFrame.FIFTEEN_MINUTES;
            case THIRTY_MINUTES -> FundamentalTimeFrame.THIRTY_MINUTES;
            case ONE_HOUR -> FundamentalTimeFrame.ONE_HOUR;
            case ONE_DAY -> FundamentalTimeFrame.ONE_DAY;
            default -> throw new IllegalArgumentException();
        };
    }
}
