package com.crypto.analysis.main.vo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class IndicatorsTransferObject {
    private double RSI;
    private double MACD;
    private double STOCHK;
    private double STOCHD;
    private double SMA;
    private double EMA;
    private double WMA;
    private double MMA;
    private double CCI;
    private double ADX;
    private double AROONUP;
    private double AROONDOWN;

    @Override
    public String toString() {
        return "\nIndicatorsTransferObject{" +
                "\nRSI=" + RSI +
                ",\n MACD=" + MACD +
                ",\n STOCHK=" + STOCHK +
                ",\n STOCHD=" + STOCHD +
                ",\n SMA=" + SMA +
                ",\n EMA=" + EMA +
                ",\n WMA=" + WMA +
                ",\n MMA=" + MMA +
                ",\n CCI=" + CCI +
                ",\n ADX=" + ADX +
                ",\n AROONUP=" + AROONUP +
                ",\n AROONDOWN=" + AROONDOWN +
                '}';
    }

    public double[] getValuesArr() {
        return new double[]{
                RSI,
                MACD,
                STOCHK,
                STOCHD,
                SMA,
                EMA,
                WMA,
                MMA,
                CCI,
                ADX,
                AROONUP,
                AROONDOWN,
        };
    }
}
