package com.crypto.analysis.main.vo;

import com.crypto.analysis.main.data_utils.select.coin.Coin;
import com.crypto.analysis.main.data_utils.select.coin.TimeFrame;
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
    private FundamentalStockObject fundamentalData;
    private double currentFundingRate;
    private double currentOpenInterest;
    private double longShortRatio;
    private double buySellRatio;
    private double BTCDomination;
    private double sentimentMean;
    private double sentimentSum;
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
                ",\n BTCDomination=" + BTCDomination +
                ",\n sentimentMean=" + sentimentMean +
                ",\n sentimentSum=" + sentimentSum +
                ",\n candle=" + candle +
                ",\n currentIndicators=" + currentIndicators +
                ",\n fundamentalData=" + fundamentalData +
                ",\n createTime=" + createTime +
                '}';
    }

    public double[] getParamArray() {
        double[] candleValues = candle.getValuesArr(); // 5
        double[] indicators = currentIndicators.getValuesArr(); // 41
        double[] coinFundValues = {currentFundingRate, currentOpenInterest, longShortRatio, buySellRatio}; // 4
        double[] volatileFundData = fundamentalData.getValuesArr(); // 6
        double[] volatileValues = {BTCDomination, sentimentMean, sentimentSum}; // 3
        double[] result = new double[candleValues.length + indicators.length + volatileFundData.length + coinFundValues.length + volatileValues.length]; // 5 + 41 + 6 + 4 + 3 = 59
        System.arraycopy(candleValues, 0, result, 0, candleValues.length);
        System.arraycopy(indicators, 0, result, candleValues.length, indicators.length);
        System.arraycopy(coinFundValues, 0, result, candleValues.length+indicators.length, coinFundValues.length);
        System.arraycopy(volatileFundData, 0, result, candleValues.length+indicators.length+coinFundValues.length, volatileFundData.length);
        System.arraycopy(volatileValues, 0, result, candleValues.length+indicators.length+coinFundValues.length+volatileFundData.length, volatileValues.length);
        return result;
    }
}
