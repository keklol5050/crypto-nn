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
    private float currentFundingRate;
    private float currentOpenInterest;
    private float longShortRatio;
    private float BTCDomination;
    private float sentimentMean;
    private float sentimentSum;
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

    public float[] getParamArray() {
        float[] candleValues = candle.getValuesArr(); // 5
        float[] cryptoFundData = cryptoFundamental.getParamArray(); // 8

        float[] movingAverages = currentIndicators.getMovingAverageValues(); // 28

        float[] indicators = currentIndicators.getIndicatorValues(); // 17
        float[] coinFundValues = {BTCDomination, currentOpenInterest, longShortRatio}; // 3

        float[] result = new float[candleValues.length + movingAverages.length + +cryptoFundData.length + indicators
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
