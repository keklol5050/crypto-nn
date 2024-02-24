package com.crypto.analysis.main.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Date;

@Getter
@JsonAutoDetect
public class Ticker24Object {
    private final String symbol;
    private final double priceChange;
    private final double priceChangePercent;
    private final double weightedAvgPrice;
    private final double lastPrice;
    private final double lastQty;
    private final double openPrice;
    private final double highPrice;
    private final double lowPrice;
    private final double volume;
    private final double quoteVolume;
    private final Date openTime;
    private final Date closeTime;
    private final long firstId;
    private final long lastId;
    private final int count;

    @JsonCreator
    public Ticker24Object(@JsonProperty("symbol") String symbol,
                          @JsonProperty("priceChange") double priceChange,
                          @JsonProperty("priceChangePercent") double priceChangePercent,
                          @JsonProperty("weightedAvgPrice") double weightedAvgPrice,
                          @JsonProperty("lastPrice") double lastPrice,
                          @JsonProperty("lastQty") double lastQty,
                          @JsonProperty("openPrice") double openPrice,
                          @JsonProperty("highPrice") double highPrice,
                          @JsonProperty("lowPrice") double lowPrice,
                          @JsonProperty("volume") double volume,
                          @JsonProperty("quoteVolume") double quoteVolume,
                          @JsonProperty("openTime") long openTime,
                          @JsonProperty("closeTime") long closeTime,
                          @JsonProperty("firstId") long firstId,
                          @JsonProperty("lastId") long lastId,
                          @JsonProperty("count") int count) {
        this.symbol = symbol;
        this.priceChange = priceChange;
        this.priceChangePercent = priceChangePercent;
        this.weightedAvgPrice = weightedAvgPrice;
        this.lastPrice = lastPrice;
        this.lastQty = lastQty;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.volume = volume;
        this.quoteVolume = quoteVolume;
        this.openTime = new Date(openTime);
        this.closeTime = new Date(closeTime);
        this.firstId = firstId;
        this.lastId = lastId;
        this.count = count;
    }

    @Override
    public String toString() {
        return "SymbolData{" +
                "symbol='" + symbol + '\'' +
                ", priceChange='" + priceChange + '\'' +
                ", priceChangePercent='" + priceChangePercent + '\'' +
                ", weightedAvgPrice='" + weightedAvgPrice + '\'' +
                ", lastPrice='" + lastPrice + '\'' +
                ", lastQty='" + lastQty + '\'' +
                ", openPrice='" + openPrice + '\'' +
                ", highPrice='" + highPrice + '\'' +
                ", lowPrice='" + lowPrice + '\'' +
                ", volume='" + volume + '\'' +
                ", quoteVolume='" + quoteVolume + '\'' +
                ", openTime=" + openTime +
                ", closeTime=" + closeTime +
                ", firstId=" + firstId +
                ", lastId=" + lastId +
                ", count=" + count +
                '}';
    }
}
