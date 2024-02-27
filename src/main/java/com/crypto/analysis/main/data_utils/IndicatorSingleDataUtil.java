package com.crypto.analysis.main.data_utils;

import com.crypto.analysis.main.enumerations.Periods;
import com.crypto.analysis.main.vo.CandleObject;
import com.crypto.analysis.main.vo.IndicatorsTransferObject;
import lombok.Getter;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.volume.OnBalanceVolumeIndicator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

@Getter
public class IndicatorSingleDataUtil {
    private final String symbol;
    private final Periods interval;
    private CandleObject lastCandle;

    public IndicatorSingleDataUtil(String symbol, Periods interval) {
        this.symbol = symbol;
        this.interval = interval;
    }
    public IndicatorsTransferObject getIndicatorsInfo() {
        IndicatorsTransferObject result = new IndicatorsTransferObject();

        TimeSeries series = getTimeSeries();

        RSIIndicator rsiIndicator = new RSIIndicator(new ClosePriceIndicator(series), 14);
        result.setRSI(rsiIndicator.getValue(series.getEndIndex()).doubleValue());

        MACDIndicator macd = new MACDIndicator(new ClosePriceIndicator(series), 12, 26);
        result.setMACD(macd.getValue(series.getEndIndex()).doubleValue());

        StochasticOscillatorKIndicator stochasticK = new StochasticOscillatorKIndicator(series, 14);
        StochasticOscillatorDIndicator stochasticD = new StochasticOscillatorDIndicator(stochasticK);
        result.setSTOCHK(stochasticK.getValue(series.getEndIndex()).doubleValue());
        result.setSTOCHD(stochasticD.getValue(series.getEndIndex()).doubleValue());

        OnBalanceVolumeIndicator obv = new OnBalanceVolumeIndicator(series);
        result.setOBV(obv.getValue(series.getEndIndex()).doubleValue());

        SMAIndicator smaIndicator = new SMAIndicator(new ClosePriceIndicator(series), 14);
        result.setSMA(smaIndicator.getValue(series.getEndIndex()).doubleValue());

        EMAIndicator emaIndicator = new EMAIndicator(new ClosePriceIndicator(series), 14);
        result.setEMA(emaIndicator.getValue(series.getEndIndex()).doubleValue());

        WMAIndicator wmaIndicator = new WMAIndicator(new ClosePriceIndicator(series), 14);
        result.setWMA(wmaIndicator.getValue(series.getEndIndex()).doubleValue());

        ADXIndicator adxIndicator = new ADXIndicator(series, 14);
        result.setADX(adxIndicator.getValue(series.getEndIndex()).doubleValue());

        AroonUpIndicator up = new AroonUpIndicator(series, 14);
        AroonDownIndicator down = new AroonDownIndicator(series, 14);
        result.setAROONUP(up.getValue(series.getEndIndex()).doubleValue());
        result.setAROONDOWN(down.getValue(series.getEndIndex()).doubleValue());

        VolumeIndicator relativeVolume = new VolumeIndicator(series, 10);
        result.setRELATIVEVOLUME(relativeVolume.getValue(series.getEndIndex()).doubleValue());

        return result;
    }

    private TimeSeries getTimeSeries() {
        LinkedList<CandleObject> candleObjects = BinanceDataUtil.getCandles(symbol, interval, 1500);
        lastCandle = candleObjects.getLast();
        return getTimeSeries(candleObjects);
    }

    public static TimeSeries getTimeSeries(List<CandleObject> candleObjects) {
        TimeSeries series = new BaseTimeSeries();
        for (CandleObject candle : candleObjects) {
            ZonedDateTime timestamp = candle.getCloseTime().toInstant().atZone(ZoneId.systemDefault());
            Bar bar = new BaseBar(timestamp, candle.getOpen(), candle.getHigh(), candle.getLow(), candle.getClose(), candle.getVolume());
            series.addBar(bar);
        }
        return series;
    }
}
