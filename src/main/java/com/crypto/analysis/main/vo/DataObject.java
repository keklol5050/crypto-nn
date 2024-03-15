package com.crypto.analysis.main.vo;

import com.crypto.analysis.main.enumerations.Coin;
import com.crypto.analysis.main.enumerations.TimeFrame;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class DataObject {
    private final Coin coin;
    private final TimeFrame interval;
    private CandleObject candle;
    private IndicatorsTransferObject currentIndicators; // індикатори
    private double currentFundingRate;
    private double currentOpenInterest;
    private double longShortRatio;
    private double buySellRatio;
    private Date createTime;

    public DataObject(Coin coin, TimeFrame interval) {
        this.coin = coin;
        this.interval = interval;
        createTime = new Date();
    }

    @Override
    public String toString() {
        return "\nDataObject{" +
                "\ncoin='" + coin + '\'' +
                ",\n interval=" + interval +
                ",\n currentFundingRate=" + currentFundingRate +
                ",\n currentOpenInterest=" + currentOpenInterest +
                ",\n longShortRatio=" + longShortRatio +
                ",\n buySellRatio=" + buySellRatio +
                ",\n candle=" + candle +
                ",\n currentIndicators=" + currentIndicators +
                ",\n createTime=" + createTime +
                '}';
    }

    public double[] getParamArray() {
        double[] candleValues = candle.getValuesArr(); // 5
        double[] indicators = currentIndicators.getValuesArr(); // 12
        double[] fundValues = {currentFundingRate, currentOpenInterest, longShortRatio, buySellRatio}; // 4
        double[] result = new double[candleValues.length + indicators.length + fundValues.length]; // 4 + 5 + 12 = 21
        System.arraycopy(candleValues, 0, result, 0, candleValues.length);
        System.arraycopy(indicators, 0, result, candleValues.length, indicators.length);
        System.arraycopy(fundValues, 0, result, candleValues.length + indicators.length, fundValues.length);
        return candleValues;
    }

    public double getMAValues() {
        return currentIndicators.getMAValues();
    }

    public double getMAValuesAverage() {
        return currentIndicators.getMAValuesAverage();
    }
    public double getUpIndicatorValues() {
        return currentIndicators.getUpIndicatorValues();
    }

    public double getUpIndicatorValuesAverage() {
        return currentIndicators.getUpIndicatorValuesAverage();
    }

    public double getDownIndicatorValues() {
        return currentIndicators.getDownIndicatorValues();
    }

    public double getDownIndicatorValuesAverage() {
        return currentIndicators.getDownIndicatorValuesAverage();
    }

    public double[] getPreparedParamArray() {
        double[] result = new double[4];
        result[0] = candle.getOpen();
        result[1] = candle.getHigh();
        result[2] = candle.getLow();
        result[3] = candle.getClose();
        return result;
    }
}
