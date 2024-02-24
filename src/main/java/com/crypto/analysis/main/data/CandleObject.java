package com.crypto.analysis.main.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Date;

@Getter
public class CandleObject { // формат свічки графіку
    private final Date openTime;
    private final String open;
    private final String high;
    private final String low;
    private final String close;
    private final String volume;
    private final Date closeTime;
    private final String quoteAssetVolume;
    private final int numberOfTrades;
    private final String takerBuyBaseAssetVolume;
    private final String takerBuyQuoteAssetVolume;

    @JsonCreator
    public CandleObject(
            @JsonProperty("0") Date openTime,
            @JsonProperty("1") String open,
            @JsonProperty("2") String high,
            @JsonProperty("3") String low,
            @JsonProperty("4") String close,
            @JsonProperty("5") String volume,
            @JsonProperty("6") Date closeTime,
            @JsonProperty("7") String quoteAssetVolume,
            @JsonProperty("8") int numberOfTrades,
            @JsonProperty("9") String takerBuyBaseAssetVolume,
            @JsonProperty("10") String takerBuyQuoteAssetVolume) {
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

    @Override
    public String toString() {
        return "CandleObject{" +
                "openTime=" + openTime +
                ",\n open='" + open + '\'' +
                ",\n high='" + high + '\'' +
                ",\n low='" + low + '\'' +
                ",\n close='" + close + '\'' +
                ",\n volume='" + volume + '\'' +
                ",\n closeTime=" + closeTime +
                ",\n quoteAssetVolume='" + quoteAssetVolume + '\'' +
                ",\n numberOfTrades=" + numberOfTrades +
                ",\n takerBuyBaseAssetVolume='" + takerBuyBaseAssetVolume + '\'' +
                ",\n takerBuyQuoteAssetVolume='" + takerBuyQuoteAssetVolume + '\'' +
                '}';
    }
}