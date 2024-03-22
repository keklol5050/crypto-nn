package com.crypto.analysis.main.vo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class IndicatorsTransferObject {
    private double RSI;

    private double MACD12;
    private double MACD24;

    private double STOCHK;
    private double STOCHD;

    private double SMA200;
    private double SMA99;
    private double SMA60;
    private double SMA50;
    private double SMA30;
    private double SMA15;
    private double SMA10;

    private double WMA200;
    private double WMA99;
    private double WMA60;
    private double WMA50;
    private double WMA30;
    private double WMA15;
    private double WMA10;

    private double EMA200;
    private double EMA99;
    private double EMA60;
    private double EMA50;
    private double EMA30;
    private double EMA15;
    private double EMA10;

    private double MMA;
    private double CCI;
    private double ADX;
    private double AROONUP;
    private double AROONDOWN;

    private double PSAR;
    private double STOCHRSI;
    private double VWAP;

    private double ATR;
    private double DPO;
    private double WILLR;
    private double MI;

    private double CMO;
    private double ROC;
    private double RAVI;

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
                '}';
    }

    public double[] getValuesArr() {
        return new double[]{
                RSI,

                MACD12,
                MACD24,

                STOCHK,
                STOCHD,

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
                CCI,
                ADX,
                AROONUP,
                AROONDOWN,

                PSAR,
                STOCHRSI,
                VWAP,

                ATR,
                DPO,
                WILLR,
                MI,

                CMO,
                ROC,
                RAVI
        };
    }


}
