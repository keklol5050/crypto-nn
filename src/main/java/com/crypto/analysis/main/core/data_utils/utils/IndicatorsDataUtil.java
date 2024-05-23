package com.crypto.analysis.main.core.data_utils.utils;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.vo.CandleObject;
import com.crypto.analysis.main.core.vo.IndicatorsTransferObject;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.aroon.AroonDownIndicator;
import org.ta4j.core.indicators.aroon.AroonUpIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.ichimoku.IchimokuKijunSenIndicator;
import org.ta4j.core.indicators.ichimoku.IchimokuSenkouSpanAIndicator;
import org.ta4j.core.indicators.ichimoku.IchimokuSenkouSpanBIndicator;
import org.ta4j.core.indicators.ichimoku.IchimokuTenkanSenIndicator;
import org.ta4j.core.indicators.volume.VWAPIndicator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class IndicatorsDataUtil {
    private final ArrayList<CandleObject> candles;
    private final boolean revert;

    private RSIIndicator rsi;

    private MACDIndicator macd12;
    private MACDIndicator macd24;

    private StochasticOscillatorKIndicator stochasticK;
    private StochasticOscillatorDIndicator stochasticD;

    private SMAIndicator smaIndicator200;
    private SMAIndicator smaIndicator99;
    private SMAIndicator smaIndicator60;
    private SMAIndicator smaIndicator50;
    private SMAIndicator smaIndicator30;
    private SMAIndicator smaIndicator15;
    private SMAIndicator smaIndicator10;

    private WMAIndicator wmaIndicator200;
    private WMAIndicator wmaIndicator99;
    private WMAIndicator wmaIndicator60;
    private WMAIndicator wmaIndicator50;
    private WMAIndicator wmaIndicator30;
    private WMAIndicator wmaIndicator15;
    private WMAIndicator wmaIndicator10;

    private EMAIndicator emaIndicator200;
    private EMAIndicator emaIndicator99;
    private EMAIndicator emaIndicator60;
    private EMAIndicator emaIndicator50;
    private EMAIndicator emaIndicator30;
    private EMAIndicator emaIndicator15;
    private EMAIndicator emaIndicator10;

    private MMAIndicator mma;
    private CCIIndicator cci;
    private ADXIndicator adxIndicator;
    private AroonUpIndicator up;
    private AroonDownIndicator down;

    private ParabolicSarIndicator psar;
    private StochasticRSIIndicator stochRSI;
    private VWAPIndicator vwap;

    private ATRIndicator atr;
    private DPOIndicator dpo;
    private WilliamsRIndicator willr;
    private MassIndexIndicator mi;

    private CMOIndicator cmo;
    private ROCIndicator roc;
    private RAVIIndicator ravi;

    private IchimokuSenkouSpanAIndicator spanA;
    private IchimokuSenkouSpanBIndicator spanB;
    private IchimokuKijunSenIndicator kijun;
    private IchimokuTenkanSenIndicator tenkan;


    public IndicatorsDataUtil(Coin coin, TimeFrame interval) {
        candles = BinanceDataUtil.getCandles(coin, interval, 1500);
        revert = true;
        init();
    }

    public IndicatorsDataUtil(ArrayList<CandleObject> candles) {
        this.candles = candles;
        revert = false;
        init();
    }

    public static BarSeries getTimeSeries(ArrayList<CandleObject> candleObjects) {
        BarSeries series = new BaseBarSeries();
        for (CandleObject candle : candleObjects) {
            ZonedDateTime timestamp = candle.getCloseTime().toInstant().atZone(ZoneId.systemDefault());
            series.addBar(timestamp, candle.getOpen(), candle.getHigh(), candle.getLow(), candle.getClose(), candle.getVolume());
        }
        return series;
    }

    private void init() {
        BarSeries series = getTimeSeries(candles);
        ClosePriceIndicator ind = new ClosePriceIndicator(series);

        rsi = new RSIIndicator(ind, 14);

        macd12 = new MACDIndicator(ind, 12, 26);
        macd24 = new MACDIndicator(ind, 24, 78);

        stochasticK = new StochasticOscillatorKIndicator(series, 14);
        stochasticD = new StochasticOscillatorDIndicator(stochasticK);

        smaIndicator200 = new SMAIndicator(ind, 200);
        smaIndicator99 = new SMAIndicator(ind, 99);
        smaIndicator60 = new SMAIndicator(ind, 60);
        smaIndicator50 = new SMAIndicator(ind, 50);
        smaIndicator30 = new SMAIndicator(ind, 30);
        smaIndicator15 = new SMAIndicator(ind, 15);
        smaIndicator10 = new SMAIndicator(ind, 10);

        wmaIndicator200 = new WMAIndicator(ind, 200);
        wmaIndicator99 = new WMAIndicator(ind, 99);
        wmaIndicator60 = new WMAIndicator(ind, 60);
        wmaIndicator50 = new WMAIndicator(ind, 50);
        wmaIndicator30 = new WMAIndicator(ind, 30);
        wmaIndicator15 = new WMAIndicator(ind, 15);
        wmaIndicator10 = new WMAIndicator(ind, 10);

        emaIndicator200 = new EMAIndicator(ind, 200);
        emaIndicator99 = new EMAIndicator(ind, 99);
        emaIndicator60 = new EMAIndicator(ind, 60);
        emaIndicator50 = new EMAIndicator(ind, 50);
        emaIndicator30 = new EMAIndicator(ind, 30);
        emaIndicator15 = new EMAIndicator(ind, 15);
        emaIndicator10 = new EMAIndicator(ind, 10);

        mma = new MMAIndicator(ind, 30);
        cci = new CCIIndicator(series, 20);
        adxIndicator = new ADXIndicator(series, 14);
        up = new AroonUpIndicator(series, 14);
        down = new AroonDownIndicator(series, 14);

        psar = new ParabolicSarIndicator(series);
        stochRSI = new StochasticRSIIndicator(rsi, 14);
        vwap = new VWAPIndicator(series, 14);

        atr = new ATRIndicator(series, 14);
        dpo = new DPOIndicator(series, 21);
        willr = new WilliamsRIndicator(series, 14);
        mi = new MassIndexIndicator(series, 9, 14);

        cmo = new CMOIndicator(ind, 9);
        roc = new ROCIndicator(ind, 9);
        ravi = new RAVIIndicator(ind, 7, 65);

        spanA = new IchimokuSenkouSpanAIndicator(series);
        spanB = new IchimokuSenkouSpanBIndicator(series);
        kijun = new IchimokuKijunSenIndicator(series);
        tenkan = new IchimokuTenkanSenIndicator(series);
    }

    public IndicatorsTransferObject getIndicators(int countBar) {
        if (revert) {
            countBar = candles.size() - 1 - countBar;
        }
        IndicatorsTransferObject result = new IndicatorsTransferObject();

        result.setRSI(rsi.getValue(countBar).floatValue());

        result.setMACD12(macd12.getValue(countBar).floatValue());
        result.setMACD24(macd24.getValue(countBar).floatValue());

        result.setSTOCHK(stochasticK.getValue(countBar).floatValue());
        result.setSTOCHD(stochasticD.getValue(countBar).floatValue());

        result.setSMA200(smaIndicator200.getValue(countBar).floatValue());
        result.setSMA99(smaIndicator99.getValue(countBar).floatValue());
        result.setSMA60(smaIndicator60.getValue(countBar).floatValue());
        result.setSMA50(smaIndicator50.getValue(countBar).floatValue());
        result.setSMA30(smaIndicator30.getValue(countBar).floatValue());
        result.setSMA15(smaIndicator15.getValue(countBar).floatValue());
        result.setSMA10(smaIndicator10.getValue(countBar).floatValue());

        result.setWMA200(wmaIndicator200.getValue(countBar).floatValue());
        result.setWMA99(wmaIndicator99.getValue(countBar).floatValue());
        result.setWMA60(wmaIndicator60.getValue(countBar).floatValue());
        result.setWMA50(wmaIndicator50.getValue(countBar).floatValue());
        result.setWMA30(wmaIndicator30.getValue(countBar).floatValue());
        result.setWMA15(wmaIndicator15.getValue(countBar).floatValue());
        result.setWMA10(wmaIndicator10.getValue(countBar).floatValue());

        result.setEMA200(emaIndicator200.getValue(countBar).floatValue());
        result.setEMA99(emaIndicator99.getValue(countBar).floatValue());
        result.setEMA60(emaIndicator60.getValue(countBar).floatValue());
        result.setEMA50(emaIndicator50.getValue(countBar).floatValue());
        result.setEMA30(emaIndicator30.getValue(countBar).floatValue());
        result.setEMA15(emaIndicator15.getValue(countBar).floatValue());
        result.setEMA10(emaIndicator10.getValue(countBar).floatValue());

        result.setMMA(mma.getValue(countBar).floatValue());
        result.setCCI(cci.getValue(countBar).floatValue());
        result.setADX(adxIndicator.getValue(countBar).floatValue());
        result.setAROONUP(up.getValue(countBar).floatValue());
        result.setAROONDOWN(down.getValue(countBar).floatValue());

        result.setPSAR(psar.getValue(countBar).floatValue());
        result.setSTOCHRSI(stochRSI.getValue(countBar).floatValue());
        result.setVWAP(vwap.getValue(countBar).floatValue());

        result.setATR(atr.getValue(countBar).floatValue());
        result.setDPO(dpo.getValue(countBar).floatValue());
        result.setWILLR(willr.getValue(countBar).floatValue());
        result.setMI(mi.getValue(countBar).floatValue());

        result.setCMO(cmo.getValue(countBar).floatValue());
        result.setROC(roc.getValue(countBar).floatValue());
        result.setRAVI(ravi.getValue(countBar).floatValue());

        result.setSPANA(spanA.getValue(countBar).floatValue());
        result.setSPANB(spanB.getValue(countBar).floatValue());
        result.setKIJUN(kijun.getValue(countBar).floatValue());
        result.setTENKAN(tenkan.getValue(countBar).floatValue());

        return result;
    }
}
