package com.crypto.analysis.main.core.vo;

import lombok.Setter;

import java.util.Date;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FundamentalStockObject that)) return false;
        return Double.compare(SPX, that.SPX) == 0 && Double.compare(DXY, that.DXY) == 0 && Double.compare(DJI, that.DJI) == 0 && Double.compare(VIX, that.VIX) == 0 && Double.compare(NDX, that.NDX) == 0 && Double.compare(GOLD, that.GOLD) == 0 && Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(createTime, SPX, DXY, DJI, VIX, NDX, GOLD);
    }
}
