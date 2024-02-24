package com.crypto.analysis.main.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

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

    private final Date createTime;

    public IndicatorsTransferObject() {
        createTime=new Date();
    }

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
                ",\n createTime=" + createTime +
                '}';
    }
}
