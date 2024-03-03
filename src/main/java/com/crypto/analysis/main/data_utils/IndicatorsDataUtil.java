package com.crypto.analysis.main.data_utils;

import com.crypto.analysis.main.enumerations.TimeFrame;
import com.crypto.analysis.main.vo.CandleObject;
import com.crypto.analysis.main.vo.IndicatorsTransferObject;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.util.List;

public class IndicatorsDataUtil {
    private final List<CandleObject> candles;
    private final boolean revert;
    private RSIIndicator rsiIndicator;
    private MACDIndicator macd;
    private StochasticOscillatorKIndicator stochasticK;
    private StochasticOscillatorDIndicator stochasticD;
    private SMAIndicator smaIndicator;
    private EMAIndicator emaIndicator;
    private WMAIndicator wmaIndicator;
    private ADXIndicator adxIndicator;
    private AroonUpIndicator up;
    private AroonDownIndicator down;
    private MMAIndicator mma;
    private CCIIndicator cci;

    public IndicatorsDataUtil(String symbol, TimeFrame interval) {
        candles = BinanceDataUtil.getCandles(symbol, interval, 1500);
        revert = true;
        init();
    }

    public IndicatorsDataUtil(List<CandleObject> candles) {
        this.candles = candles;
        revert = false;
        init();
    }

    private void init() {
        TimeSeries series = IndicatorSingleDataUtil.getTimeSeries(candles);

        rsiIndicator = new RSIIndicator(new ClosePriceIndicator(series), 14);
        macd = new MACDIndicator(new ClosePriceIndicator(series), 12, 26);
        stochasticK = new StochasticOscillatorKIndicator(series, 14);
        stochasticD = new StochasticOscillatorDIndicator(stochasticK);
        smaIndicator = new SMAIndicator(new ClosePriceIndicator(series), 14);
        emaIndicator = new EMAIndicator(new ClosePriceIndicator(series), 14);
        wmaIndicator = new WMAIndicator(new ClosePriceIndicator(series), 14);
        mma = new MMAIndicator(new ClosePriceIndicator(series), 14);
        cci = new CCIIndicator(series, 14);
        adxIndicator = new ADXIndicator(series, 14);
        up = new AroonUpIndicator(series, 14);
        down = new AroonDownIndicator(series, 14);
    }

    public IndicatorsTransferObject getIndicators(int countBar) {
        if (revert) {
            countBar = candles.size() - 1 - countBar;
        }
        IndicatorsTransferObject result = new IndicatorsTransferObject();

        result.setRSI(rsiIndicator.getValue(countBar).doubleValue());
        result.setMACD(macd.getValue(countBar).doubleValue());
        result.setSTOCHK(stochasticK.getValue(countBar).doubleValue());
        result.setSTOCHD(stochasticD.getValue(countBar).doubleValue());
        result.setSMA(smaIndicator.getValue(countBar).doubleValue());
        result.setEMA(emaIndicator.getValue(countBar).doubleValue());
        result.setWMA(wmaIndicator.getValue(countBar).doubleValue());
        result.setADX(adxIndicator.getValue(countBar).doubleValue());
        result.setAROONUP(up.getValue(countBar).doubleValue());
        result.setAROONDOWN(down.getValue(countBar).doubleValue());
        result.setMMA(mma.getValue(countBar).doubleValue());
        result.setCCI(cci.getValue(countBar).doubleValue());

        return result;
    }
}
