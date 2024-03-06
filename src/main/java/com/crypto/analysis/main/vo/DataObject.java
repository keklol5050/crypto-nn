package com.crypto.analysis.main.vo;

import com.crypto.analysis.main.enumerations.TimeFrame;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class DataObject {
    private final String symbol;
    private final TimeFrame interval;
    private CandleObject candle;
    private IndicatorsTransferObject currentIndicators; // індикатори

    private Date createTime;

    public DataObject(String symbol, TimeFrame interval) {
        this.symbol = symbol;
        this.interval = interval;
        createTime = new Date();
    }

    @Override
    public String toString() {
        return "\nDataObject{" +
                "\nsymbol='" + symbol + '\'' +
                ",\n interval=" + interval +
                ",\n candle=" + candle +
                ",\n currentIndicators=" + currentIndicators +
                ",\n createTime=" + createTime +
                '}';
    }

    public double[] getParamArray() {
        double[] candleValues = candle.getValuesArr();
        double[] indicators = currentIndicators.getValuesArr();
        double[] result = new double[candleValues.length + indicators.length];
        System.arraycopy(candleValues, 0, result, 0, candleValues.length);
        System.arraycopy(indicators, 0, result, candleValues.length, indicators.length);
        return result;
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
        double[] result = new double[5];
        result[0] = candle.getLow();
        result[1] = candle.getHigh();
        result[2] = candle.getClose();
        result[3] = getMAValuesAverage();
        result[4] = getUpIndicatorValues();
        return result;
    }
}
