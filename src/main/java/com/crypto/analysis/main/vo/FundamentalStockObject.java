package com.crypto.analysis.main.vo;

import com.crypto.analysis.main.data_utils.select.fundamental.FundamentalTimeFrame;
import lombok.Setter;

import java.util.Date;

@Setter
public class FundamentalStockObject {
    private double SPX;
    private double DXY;
    private double DJI;
    private double VIX;
    private double NDX;
    private double GOLD;

    private final FundamentalTimeFrame interval;
    private final Date createTime;

    public FundamentalStockObject(FundamentalTimeFrame interval) {
        this.interval = interval;
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
                ", interval=" + interval +
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
