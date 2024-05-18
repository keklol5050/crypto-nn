package com.crypto.analysis.main.core.vo;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Objects;

@Setter
@Getter
public class DataObject {
    private final Coin coin;
    private final TimeFrame interval;
    private CandleObject candle;
    private IndicatorsTransferObject currentIndicators; // індикатори
    private FundamentalStockObject fundamentalData;
    private FundamentalCryptoDataObject cryptoFundamental;
    private double currentFundingRate;
    private double currentOpenInterest;
    private double longShortRatio;
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
                ",\n BTCDomination=" + BTCDomination +
                ",\n sentimentMean=" + sentimentMean +
                ",\n sentimentSum=" + sentimentSum +
                ",\n candle=" + candle +
                ",\n currentIndicators=" + currentIndicators +
                ",\n fundamentalData=" + fundamentalData +
                ",\n cryptoFundamental=" + cryptoFundamental +
                ",\n createTime=" + createTime +
                '}';
    }

    public double[] getParamArray() {
        double[] candleValues = candle.getValuesArr(); // 5
        double[] cryptoFundData = cryptoFundamental.getParamArray(); // 8

        double[] movingAverages = currentIndicators.getMovingAverageValues(); // 28

        double[] indicators = currentIndicators.getIndicatorValues(); // 17
        double[] coinFundValues = {BTCDomination, currentOpenInterest, longShortRatio}; // 3

        double[] result = new double[candleValues.length + movingAverages.length + +cryptoFundData.length + indicators
                .length + coinFundValues.length]; // 5 + 8 + 28  + 17 + 3 = 61

        System.arraycopy(candleValues, 0, result,
                0, candleValues.length);
        System.arraycopy(cryptoFundData, 0, result,
                candleValues.length, cryptoFundData.length);
        System.arraycopy(movingAverages, 0, result,
                candleValues.length + cryptoFundData.length, movingAverages.length);
        System.arraycopy(indicators, 0, result,
                candleValues.length + cryptoFundData.length + movingAverages.length, indicators.length);
        System.arraycopy(coinFundValues, 0, result,
                candleValues.length + cryptoFundData.length + movingAverages.length + indicators.length, coinFundValues.length);

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataObject that)) return false;
        return Double.compare(getCurrentFundingRate(), that.getCurrentFundingRate()) == 0 && Double.compare(getCurrentOpenInterest(), that.getCurrentOpenInterest()) == 0 && Double.compare(getLongShortRatio(), that.getLongShortRatio()) == 0 && Double.compare(getBTCDomination(), that.getBTCDomination()) == 0 && Double.compare(getSentimentMean(), that.getSentimentMean()) == 0 && Double.compare(getSentimentSum(), that.getSentimentSum()) == 0 && getCoin() == that.getCoin() && getInterval() == that.getInterval() && Objects.equals(getCandle(), that.getCandle()) && Objects.equals(getCurrentIndicators(), that.getCurrentIndicators()) && Objects.equals(getFundamentalData(), that.getFundamentalData()) && Objects.equals(getCryptoFundamental(), that.getCryptoFundamental()) && Objects.equals(getCreateTime(), that.getCreateTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCoin(), getInterval(), getCandle(), getCurrentIndicators(), getFundamentalData(), getCryptoFundamental(), getCurrentFundingRate(), getCurrentOpenInterest(), getLongShortRatio(), getBTCDomination(), getSentimentMean(), getSentimentSum(), getCreateTime());
    }
}
