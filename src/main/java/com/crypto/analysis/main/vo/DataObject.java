package com.crypto.analysis.main.vo;

import com.crypto.analysis.main.data.CandleObject;
import com.crypto.analysis.main.data.Ticker24Object;
import com.crypto.analysis.main.data.TickerBookObject;
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
    private Date date;

    public DataObject(String symbol) {
        this.symbol = symbol;
        date = new Date();
    }

    @Override
    public String toString() {
        return "DataObject{" +
                "symbol='" + symbol + '\'' +
                ",\n candles=" + candles +
                ",\n currentOpenInterest=" + currentOpenInterest +
                ",\n currentLongShortRatio='" + currentLongShortRatio + '\'' +
                ",\n currentTopTradersLongShortRatio='" + currentTopTradersLongShortRatio + '\'' +
                ",\n currentFundingRate=" + currentFundingRate +
                ",\n currentBuySellRatioAndVolumes='" + currentBuySellRatioAndVolumes + '\'' +
                ",\n tickerBookObject=" + tickerBookObject +
                ",\n ticker24Object=" + ticker24Object +
                ",\n date=" + date +
                '}';
    }
}
