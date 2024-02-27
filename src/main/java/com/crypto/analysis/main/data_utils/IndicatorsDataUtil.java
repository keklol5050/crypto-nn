package com.crypto.analysis.main.data_utils;

import com.crypto.analysis.main.enumerations.Periods;
import com.crypto.analysis.main.vo.CandleObject;
import com.crypto.analysis.main.vo.IndicatorsTransferObject;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.volume.OnBalanceVolumeIndicator;

import java.util.LinkedList;

public class IndicatorsDataUtil {
    LinkedList<CandleObject> candles;

    public IndicatorsDataUtil(String symbol, Periods interval) {
        candles = BinanceDataUtil.getCandles(symbol, interval, 1500);
    }

    public IndicatorsTransferObject getIndicators( int countBar) {
        TimeSeries series = IndicatorSingleDataUtil.getTimeSeries(candles);
        countBar = candles.size()-1-countBar;
        IndicatorsTransferObject result = new IndicatorsTransferObject();

        RSIIndicator rsiIndicator = new RSIIndicator(new ClosePriceIndicator(series), 14);
        result.setRSI(rsiIndicator.getValue(countBar).doubleValue());

        MACDIndicator macd = new MACDIndicator(new ClosePriceIndicator(series), 12, 26);
        result.setMACD(macd.getValue(countBar).doubleValue());

        StochasticOscillatorKIndicator stochasticK = new StochasticOscillatorKIndicator(series, 14);
        StochasticOscillatorDIndicator stochasticD = new StochasticOscillatorDIndicator(stochasticK);
        result.setSTOCHK(stochasticK.getValue(countBar).doubleValue());
        result.setSTOCHD(stochasticD.getValue(countBar).doubleValue());

        OnBalanceVolumeIndicator obv = new OnBalanceVolumeIndicator(series);
        result.setOBV(obv.getValue(countBar).doubleValue());

        SMAIndicator smaIndicator = new SMAIndicator(new ClosePriceIndicator(series), 14);
        result.setSMA(smaIndicator.getValue(countBar).doubleValue());

        EMAIndicator emaIndicator = new EMAIndicator(new ClosePriceIndicator(series), 14);
        result.setEMA(emaIndicator.getValue(countBar).doubleValue());

        WMAIndicator wmaIndicator = new WMAIndicator(new ClosePriceIndicator(series), 14);
        result.setWMA(wmaIndicator.getValue(countBar).doubleValue());

        ADXIndicator adxIndicator = new ADXIndicator(series, 14);
        result.setADX(adxIndicator.getValue(countBar).doubleValue());

        AroonUpIndicator up = new AroonUpIndicator(series, 14);
        AroonDownIndicator down = new AroonDownIndicator(series, 14);
        result.setAROONUP(up.getValue(countBar).doubleValue());
        result.setAROONDOWN(down.getValue(countBar).doubleValue());

        VolumeIndicator relativeVolume = new VolumeIndicator(series, 10);
        result.setRELATIVEVOLUME(relativeVolume.getValue(countBar).doubleValue());

        return result;
    }
}
