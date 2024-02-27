package com.crypto.analysis.main.vo;

import com.crypto.analysis.main.enumerations.Periods;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Date;

@Setter
@Getter
public class DataObject {
    private final String symbol;
    private final Periods interval;
    private CandleObject candle;
    private IndicatorsTransferObject currentIndicators; // індикатори

    private Date createTime;

    public DataObject(String symbol, Periods interval) {
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
        double[] indicators =currentIndicators.getValuesArr();
        double[] result = new double[candleValues.length + indicators.length + 1];
        System.arraycopy(candleValues, 0, result, 0, candleValues.length);
        System.arraycopy(indicators, 0, result, candleValues.length, indicators.length);
        result[result.length - 1] = interval.ordinal()+1;
        return result;
    }

}
