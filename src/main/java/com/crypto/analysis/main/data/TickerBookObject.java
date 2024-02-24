package com.crypto.analysis.main.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Date;

@Getter
@JsonAutoDetect
public class TickerBookObject {
    private final String symbol;
    private final double bidPrice;
    private final double bidQty;
    private final double askPrice;
    private final double askQty;
    private final Date time;
    private final long lastUpdateId;

    @JsonCreator
    public TickerBookObject(@JsonProperty("0") String symbol,
                            @JsonProperty("1") double bidPrice,
                            @JsonProperty("2") double bidQty,
                            @JsonProperty("3") double askPrice,
                            @JsonProperty("4") double askQty,
                            @JsonProperty("6") long time,
                            @JsonProperty("7") long lastUpdateId) {
        this.symbol = symbol;
        this.bidPrice = bidPrice;
        this.bidQty = bidQty;
        this.askPrice = askPrice;
        this.askQty = askQty;
        this.time = new Date(time);
        this.lastUpdateId = lastUpdateId;
    }

    @Override
    public String toString() {
        return "TickerBookObject{" +
                "symbol='" + symbol + '\'' +
                ", bidPrice=" + bidPrice +
                ", bidQty=" + bidQty +
                ", askPrice=" + askPrice +
                ", askQty=" + askQty +
                ", time=" + time +
                ", lastUpdateId=" + lastUpdateId +
                '}';
    }
}
