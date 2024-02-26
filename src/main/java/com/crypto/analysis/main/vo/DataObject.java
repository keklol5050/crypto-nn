package com.crypto.analysis.main.vo;

import com.crypto.analysis.main.enumerations.Periods;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

@Setter
@Getter
public class DataObject {
    private final String symbol;
    private final Periods interval;
    private CandleObject candle;
    private double currentOpenInterest;
    private double longRatio;
    private double shortRatio;
    private String currentTopTradersLongShortRatio; // формат long=short
    private double currentFundingRate;
    private String currentBuySellRatioAndVolumes; // формат buySellRatio=buyVol-sellVol
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
                ",\n candle=" + candle +
                ",\n currentOpenInterest=" + currentOpenInterest +
                ",\n longRatio=" + longRatio +
                ",\n shortRatio=" + shortRatio +
                ",\n currentFundingRate=" + currentFundingRate + '}';
    }

    public double[] getParamArray() {
        double[] candleValues = Arrays.stream(candle.getValuesArr()).map(e->e/1000).toArray();
        double[] indicators = Arrays.stream(currentIndicators.getValuesArr()).map(e->e/1000).toArray();
        double[] fundingAndOI = {interval.ordinal()+1, currentFundingRate*1000, currentOpenInterest/10000, longRatio, shortRatio};
        double[] result = new double[candleValues.length + fundingAndOI.length + indicators.length];
        System.arraycopy(candleValues, 0, result, 0, candleValues.length);
        System.arraycopy(fundingAndOI, 0, result, candleValues.length, fundingAndOI.length);
        System.arraycopy(indicators, 0, result, fundingAndOI.length + candleValues.length, indicators.length);
        return result;
    }

}
