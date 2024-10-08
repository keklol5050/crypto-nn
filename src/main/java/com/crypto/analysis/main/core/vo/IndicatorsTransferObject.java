package com.crypto.analysis.main.core.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
public class IndicatorsTransferObject {
    private float RSI;

    private float MACD12;
    private float MACD24;

    private float STOCHK;
    private float STOCHD;

    private float SMA200;
    private float SMA99;
    private float SMA60;
    private float SMA50;
    private float SMA30;
    private float SMA15;
    private float SMA10;

    private float WMA200;
    private float WMA99;
    private float WMA60;
    private float WMA50;
    private float WMA30;
    private float WMA15;
    private float WMA10;

    private float EMA200;
    private float EMA99;
    private float EMA60;
    private float EMA50;
    private float EMA30;
    private float EMA15;
    private float EMA10;

    private float MMA;
    private float CCI;
    private float ADX;
    private float AROONUP;
    private float AROONDOWN;

    private float PSAR;
    private float STOCHRSI;
    private float VWAP;

    private float ATR;
    private float DPO;
    private float WILLR;
    private float MI;

    private float CMO;
    private float ROC;
    private float RAVI;

    private float SPANA;
    private float SPANB;
    private float KIJUN;
    private float TENKAN;

    @Override
    public String toString() {
        return "IndicatorsTransferObject{" +
                "RSI=" + RSI +
                ", MACD12=" + MACD12 +
                ", MACD24=" + MACD24 +
                ", STOCHK=" + STOCHK +
                ", STOCHD=" + STOCHD +
                ", SMA200=" + SMA200 +
                ", SMA99=" + SMA99 +
                ", SMA60=" + SMA60 +
                ", SMA50=" + SMA50 +
                ", SMA30=" + SMA30 +
                ", SMA15=" + SMA15 +
                ", SMA10=" + SMA10 +
                ", WMA200=" + WMA200 +
                ", WMA99=" + WMA99 +
                ", WMA60=" + WMA60 +
                ", WMA50=" + WMA50 +
                ", WMA30=" + WMA30 +
                ", WMA15=" + WMA15 +
                ", WMA10=" + WMA10 +
                ", EMA200=" + EMA200 +
                ", EMA99=" + EMA99 +
                ", EMA60=" + EMA60 +
                ", EMA50=" + EMA50 +
                ", EMA30=" + EMA30 +
                ", EMA15=" + EMA15 +
                ", EMA10=" + EMA10 +
                ", MMA=" + MMA +
                ", CCI=" + CCI +
                ", ADX=" + ADX +
                ", AROONUP=" + AROONUP +
                ", AROONDOWN=" + AROONDOWN +
                ", PSAR=" + PSAR +
                ", STOCHRSI=" + STOCHRSI +
                ", VWAP=" + VWAP +
                ", ATR=" + ATR +
                ", DPO=" + DPO +
                ", WILLR=" + WILLR +
                ", MI=" + MI +
                ", CMO=" + CMO +
                ", ROC=" + ROC +
                ", RAVI=" + RAVI +
                ", SPANA=" + SPANA +
                ", SPANB=" + SPANB +
                ", KIJUN=" + KIJUN +
                ", TENKAN=" + TENKAN +
                '}';
    }

    public float[] getIndicatorValues() {
        return new float[]{
                RSI,

                MACD12,
                MACD24,

                STOCHK,
                STOCHD,

                CCI,
                ADX,
                AROONUP,
                AROONDOWN,
                STOCHRSI,

                ATR,
                DPO,
                WILLR,
                MI,

                CMO,
                ROC,
                RAVI
        };
    }

    public float[] getMovingAverageValues() {
        return new float[]{
                SMA200,
                SMA99,
                SMA60,
                SMA50,
                SMA30,
                SMA15,
                SMA10,

                WMA200,
                WMA99,
                WMA60,
                WMA50,
                WMA30,
                WMA15,
                WMA10,

                EMA200,
                EMA99,
                EMA60,
                EMA50,
                EMA30,
                EMA15,
                EMA10,

                MMA,
                PSAR,
                VWAP,

                SPANA,
                SPANB,
                KIJUN,
                TENKAN
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndicatorsTransferObject that)) return false;
        return Double.compare(getRSI(), that.getRSI()) == 0 && Double.compare(getMACD12(), that.getMACD12()) == 0 && Double.compare(getMACD24(), that.getMACD24()) == 0 && Double.compare(getSTOCHK(), that.getSTOCHK()) == 0 && Double.compare(getSTOCHD(), that.getSTOCHD()) == 0 && Double.compare(getSMA200(), that.getSMA200()) == 0 && Double.compare(getSMA99(), that.getSMA99()) == 0 && Double.compare(getSMA60(), that.getSMA60()) == 0 && Double.compare(getSMA50(), that.getSMA50()) == 0 && Double.compare(getSMA30(), that.getSMA30()) == 0 && Double.compare(getSMA15(), that.getSMA15()) == 0 && Double.compare(getSMA10(), that.getSMA10()) == 0 && Double.compare(getWMA200(), that.getWMA200()) == 0 && Double.compare(getWMA99(), that.getWMA99()) == 0 && Double.compare(getWMA60(), that.getWMA60()) == 0 && Double.compare(getWMA50(), that.getWMA50()) == 0 && Double.compare(getWMA30(), that.getWMA30()) == 0 && Double.compare(getWMA15(), that.getWMA15()) == 0 && Double.compare(getWMA10(), that.getWMA10()) == 0 && Double.compare(getEMA200(), that.getEMA200()) == 0 && Double.compare(getEMA99(), that.getEMA99()) == 0 && Double.compare(getEMA60(), that.getEMA60()) == 0 && Double.compare(getEMA50(), that.getEMA50()) == 0 && Double.compare(getEMA30(), that.getEMA30()) == 0 && Double.compare(getEMA15(), that.getEMA15()) == 0 && Double.compare(getEMA10(), that.getEMA10()) == 0 && Double.compare(getMMA(), that.getMMA()) == 0 && Double.compare(getCCI(), that.getCCI()) == 0 && Double.compare(getADX(), that.getADX()) == 0 && Double.compare(getAROONUP(), that.getAROONUP()) == 0 && Double.compare(getAROONDOWN(), that.getAROONDOWN()) == 0 && Double.compare(getPSAR(), that.getPSAR()) == 0 && Double.compare(getSTOCHRSI(), that.getSTOCHRSI()) == 0 && Double.compare(getVWAP(), that.getVWAP()) == 0 && Double.compare(getATR(), that.getATR()) == 0 && Double.compare(getDPO(), that.getDPO()) == 0 && Double.compare(getWILLR(), that.getWILLR()) == 0 && Double.compare(getMI(), that.getMI()) == 0 && Double.compare(getCMO(), that.getCMO()) == 0 && Double.compare(getROC(), that.getROC()) == 0 && Double.compare(getRAVI(), that.getRAVI()) == 0 && Double.compare(getSPANA(), that.getSPANA()) == 0 && Double.compare(getSPANB(), that.getSPANB()) == 0 && Double.compare(getKIJUN(), that.getKIJUN()) == 0 && Double.compare(getTENKAN(), that.getTENKAN()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRSI(), getMACD12(), getMACD24(), getSTOCHK(), getSTOCHD(), getSMA200(), getSMA99(), getSMA60(), getSMA50(), getSMA30(), getSMA15(), getSMA10(), getWMA200(), getWMA99(), getWMA60(), getWMA50(), getWMA30(), getWMA15(), getWMA10(), getEMA200(), getEMA99(), getEMA60(), getEMA50(), getEMA30(), getEMA15(), getEMA10(), getMMA(), getCCI(), getADX(), getAROONUP(), getAROONDOWN(), getPSAR(), getSTOCHRSI(), getVWAP(), getATR(), getDPO(), getWILLR(), getMI(), getCMO(), getROC(), getRAVI(), getSPANA(), getSPANB(), getKIJUN(), getTENKAN());
    }
}
