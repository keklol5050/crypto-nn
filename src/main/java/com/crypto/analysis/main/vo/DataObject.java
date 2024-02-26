package com.crypto.analysis.main.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

@Setter
@Getter
public class DataObject {
    private String symbol;
    private LinkedList<CandleObject> candles; // графік, парсинг у класі BinanceDataUtil
    private double currentOpenInterest;
    private double longRatio;
    private double shortRatio;
    private String currentTopTradersLongShortRatio; // формат long=short
    private double currentFundingRate;
    private String currentBuySellRatioAndVolumes; // формат buySellRatio=buyVol-sellVol
    private IndicatorsTransferObject currentIndicators; // індикатори

    private Date createTime;

    public DataObject(String symbol) {
        this.symbol = symbol;
        createTime = new Date();
    }


    private double[][] getCandlesValues() {
        double[][] candlesValues = new double[candles.size()][5];
        for (int i = 0; i < candles.size(); i++) {
            candlesValues[i] = candles.get(i).getValuesArr();
        }
        return candlesValues;
    }

    @Override
    public String toString() {
        return "\nDataObject{" +
                "\nsymbol='" + symbol + '\'' +
                ",\n candles=" + candles +
                ",\n currentOpenInterest=" + currentOpenInterest +
                ",\n longRatio=" + longRatio +
                ",\n shortRatio=" + shortRatio +
                ",\n currentTopTradersLongShortRatio='" + currentTopTradersLongShortRatio + '\'' +
                ",\n currentFundingRate=" + currentFundingRate +
                ",\n currentBuySellRatioAndVolumes='" + currentBuySellRatioAndVolumes + '\'' +
                ",\n currentIndicators=" + currentIndicators +
                ",\n createTime=" + createTime +
                '}';
    }

    public double[] getParamArray() {
        double[] candlesValues =Arrays.stream( getCandlesValues())
                .flatMapToDouble(Arrays::stream)
                .toArray();
        double[] indicators = currentIndicators.getValuesArr();
        double[] fundingAndOI = {currentOpenInterest, currentFundingRate, longRatio, shortRatio};
        double[] result = new double[candlesValues.length + fundingAndOI.length + indicators.length];
        System.arraycopy(fundingAndOI, 0, result, 0, fundingAndOI.length);
        System.arraycopy(indicators, 0, result, fundingAndOI.length, indicators.length);
        System.arraycopy(candlesValues, 0, result, fundingAndOI.length + indicators.length, candlesValues.length);
        return Arrays.stream(candlesValues).map(e->e/1000).toArray();
    }

    public double getResultCandleCloseValue() {
        return candles.getLast().getClose();
    }
}
