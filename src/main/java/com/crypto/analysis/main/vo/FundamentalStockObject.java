package com.crypto.analysis.main.vo;

import lombok.Setter;

import java.util.Date;

@Setter
public class FundamentalStockObject {
    private final Date createTime;
    private double SPX;
    private double DXY;
    private double DJI;
    private double VIX;
    private double NDX;
    private double GOLD;

    public FundamentalStockObject() {
        createTime = new Date();
    }

    @Override
    public String toString() {
        return "FundamentalStockObject{" +
                "SPX=" + SPX +
                ", DXY=" + DXY +
                ", DJI=" + DJI +
                ", VIX=" + VIX +
                ", NDX=" + NDX +
                ", GOLD=" + GOLD +
                ", createTime=" + createTime +
                '}';
    }

    public double[] getValuesArr() {
        return new double[]{
                SPX,
                DXY,
                DJI,
                VIX,
                NDX,
                GOLD
        };
    }
}
