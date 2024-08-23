package com.crypto.analysis.main.core.vo;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.PropertiesUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
public class DataObject {
    public static final int POSITION_OF_PRICES_NORMALIZER_IND = 3;
    public static final int COUNT_PRICES_VALUES = 4;
    public static final int COUNT_VALUES_NOT_VOLATILE_WITHOUT_MA = 13;
    public static final int MOVING_AVERAGES_COUNT = 28;
    public static final int BTCDOM_POSITION = 58;
    public static final int OPEN_INTEREST_POSITION = 59;
    public static final int LONG_SHORT_RATIO_POSITION = 60;
    public static final int[] MASK_OUTPUT = new int[] {3}; // C

    public static final int SKIP_NUMBER = PropertiesUtil.getPropertyAsInteger("model.skip_number");
    public static final int COUNT_TEST_VALUES = PropertiesUtil.getPropertyAsInteger("model.test_values");

    private final Coin coin;
    private final TimeFrame interval;
    private CandleObject candle;
    private IndicatorsTransferObject currentIndicators;
    private FundamentalStockObject fundamentalData;
    private FundamentalCryptoDataObject cryptoFundamental;
    private float currentFundingRate;
    private float currentOpenInterest;
    private float longShortRatio;
    private float ETHBTCPrice;
    private float BTCDomination;
    private float sentimentMean;
    private float sentimentSum;

    public DataObject(Coin coin, TimeFrame interval) {
        this.coin = coin;
        this.interval = interval;
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
                '}';
    }

    public float[] getParamArray() {
        float[] candleValues = candle.getValuesArr(); // 5
        float[] cryptoFundData = cryptoFundamental.getParamArray();

        float[] movingAverages = currentIndicators.getMovingAverageValues(); // 28

        float[] indicators = currentIndicators.getIndicatorValues(); // 17
        float[] coinFundValues = {ETHBTCPrice, BTCDomination,currentOpenInterest, longShortRatio, currentFundingRate, sentimentMean, sentimentSum}; // 7

        float[] stockValues = fundamentalData.getValuesArr(); // 6

        float[] result = new float[candleValues.length + movingAverages.length + cryptoFundData.length + indicators
                .length + coinFundValues.length + stockValues.length];

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
        System.arraycopy(stockValues, 0, result,
                candleValues.length + cryptoFundData.length + movingAverages.length + indicators.length + coinFundValues.length, stockValues.length);

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataObject that)) return false;
        return Double.compare(getCurrentFundingRate(), that.getCurrentFundingRate()) == 0 && Double.compare(getCurrentOpenInterest(), that.getCurrentOpenInterest()) == 0 && Double.compare(getLongShortRatio(), that.getLongShortRatio()) == 0 && Double.compare(getBTCDomination(), that.getBTCDomination()) == 0 && Double.compare(getSentimentMean(), that.getSentimentMean()) == 0 && Double.compare(getSentimentSum(), that.getSentimentSum()) == 0 && getCoin() == that.getCoin() && getInterval() == that.getInterval() && Objects.equals(getCandle(), that.getCandle()) && Objects.equals(getCurrentIndicators(), that.getCurrentIndicators()) && Objects.equals(getFundamentalData(), that.getFundamentalData()) && Objects.equals(getCryptoFundamental(), that.getCryptoFundamental());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCoin(), getInterval(), getCandle(), getCurrentIndicators(), getFundamentalData(), getCryptoFundamental(), getCurrentFundingRate(), getCurrentOpenInterest(), getLongShortRatio(), getBTCDomination(), getSentimentMean(), getSentimentSum());
    }
}
