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
    private double OBV;
    private double SMA;
    private double EMA;
    private double WMA;
    private double ADX;
    private double AROONUP;
    private double AROONDOWN;
    private double RELATIVEVOLUME;

    @Override
    public String toString() {
        return "IndicatorsTransferObject{" +
                "\nRSI=" + RSI +
                ",\n MACD=" + MACD +
                ",\n STOCHK=" + STOCHK +
                ",\n STOCHD=" + STOCHD +
                ",\n OBV=" + OBV +
                ",\n SMA=" + SMA +
                ",\n EMA=" + EMA +
                ",\n WMA=" + WMA +
                ",\n ADX=" + ADX +
                ",\n AROONUP=" + AROONUP +
                ",\n AROONDOWN=" + AROONDOWN +
                ",\n RELATIVEVOLUME=" + RELATIVEVOLUME +
                '}';
    }

    public double[] getValuesArr(){
        return new double[]{
                RSI/100,
                MACD/1000,
                STOCHK/100,
                STOCHD/100,
                OBV/100000,
                SMA/10000,
                EMA/10000,
                WMA/10000,
                ADX/100,
                AROONUP/100,
                AROONDOWN/100,
                RELATIVEVOLUME/100000
        };
    }
}
