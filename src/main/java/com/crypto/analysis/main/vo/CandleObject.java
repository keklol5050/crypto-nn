package com.crypto.analysis.main.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Date;

@Getter
public class CandleObject { // формат свічки графіку
    private final Date openTime;
    private final double open;
    private final double high;
    private final double low;
    private final double close;
    private final double volume;
    private final Date closeTime;
    private final double quoteAssetVolume;
    private final int numberOfTrades;
    private final double takerBuyBaseAssetVolume;
    private final double takerBuyQuoteAssetVolume;

    @JsonCreator
    public CandleObject(
            @JsonProperty("0") Date openTime,
            @JsonProperty("1") double open,
            @JsonProperty("2") double high,
            @JsonProperty("3") double low,
            @JsonProperty("4") double close,
            @JsonProperty("5") double volume,
            @JsonProperty("6") Date closeTime,
            @JsonProperty("7") double quoteAssetVolume,
            @JsonProperty("8") int numberOfTrades,
            @JsonProperty("9") double takerBuyBaseAssetVolume,
            @JsonProperty("10") double takerBuyQuoteAssetVolume) {
        this.openTime = openTime;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.closeTime = closeTime;
        this.quoteAssetVolume = quoteAssetVolume;
        this.numberOfTrades = numberOfTrades;
        this.takerBuyBaseAssetVolume = takerBuyBaseAssetVolume;
        this.takerBuyQuoteAssetVolume = takerBuyQuoteAssetVolume;
    }


    public CandleObject(Date openTime, double open, double high, double low, double close, double volume, Date closeTime) {
        this.openTime = openTime;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;

        this.closeTime = closeTime;
        this.quoteAssetVolume = 0;
        this.numberOfTrades = 0;
        this.takerBuyBaseAssetVolume = 0;
        this.takerBuyQuoteAssetVolume = 0;
    }

    @Override
    public String toString() {
        return "\nCandleObject{" +
                "\nopenTime=" + openTime +
                ",\n open=" + open +
                ",\n high=" + high +
                ",\n low=" + low +
                ",\n close=" + close +
                ",\n closeTime=" + closeTime +
                "\n}";
    }

    public double[] getValuesArr() {
        return new double[]{
                open,
                high,
                low,
                close
        };
    }
}