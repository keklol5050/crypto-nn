package com.crypto.analysis.main.vo;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;
import java.util.List;

@Setter
@Getter
public class DataObject {
    private String symbol;
    private List<CandleObject> candles; // графік, парсинг у класі BinanceDataUtil
    private double currentOpenInterest;
    private String currentLongShortRatio; // формат long=short
    private String currentTopTradersLongShortRatio; // формат long=short
    private double currentFundingRate;
    private String currentBuySellRatioAndVolumes; // формат buySellRatio=buyVol-sellVol
    private TickerBookObject tickerBookObject;
    private Ticker24Object ticker24Object;
    private IndicatorsTransferObject currentIndicators; // індикатори

    private Date createTime;

    public DataObject(String symbol) {
        this.symbol = symbol;
        createTime = new Date();
    }

    @Override
    public String toString() {
        return "DataObject{" +
                "\nsymbol='" + symbol + '\'' +
                ",\n\t candles=" + candles +
                ",\n currentOpenInterest=" + currentOpenInterest +
                ",\n currentLongShortRatio='" + currentLongShortRatio + '\'' +
                ",\n currentTopTradersLongShortRatio='" + currentTopTradersLongShortRatio + '\'' +
                ",\n currentFundingRate=" + currentFundingRate +
                ",\n currentBuySellRatioAndVolumes='" + currentBuySellRatioAndVolumes + '\'' +
                ",\n tickerBookObject=" + tickerBookObject +
                ",\n ticker24Object=" + ticker24Object +
                ",\n\t currentIndicators=" + currentIndicators +
                ",\n createTime=" + createTime +
                "\n}";
    }
}
