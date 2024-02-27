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
        return new double[]{RSI,MACD,STOCHK,STOCHD,OBV,SMA,EMA,WMA,ADX,AROONUP,AROONDOWN,RELATIVEVOLUME};
    }
}
