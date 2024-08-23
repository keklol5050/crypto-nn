package com.crypto.analysis.main.core.vo;

import lombok.Setter;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

@Setter
public class FundamentalStockObject {
    private final Date createTime;
    private float[] SPX;
    private float[] DXY;
    private float[] DJI;
    private float[] VIX;
    private float[] NDX;
    private float[] GOLD;

    public FundamentalStockObject() {
        createTime = new Date();
    }

    @Override
    public String toString() {
        return "FundamentalStockObject{" +
                "createTime=" + createTime +
                ", SPX=" + Arrays.toString(SPX) +
                ", DXY=" + Arrays.toString(DXY) +
                ", DJI=" + Arrays.toString(DJI) +
                ", VIX=" + Arrays.toString(VIX) +
                ", NDX=" + Arrays.toString(NDX) +
                ", GOLD=" + Arrays.toString(GOLD) +
                '}';
    }

    public float[] getValuesArr() {
        return new float[]{
                SPX[3],
                DXY[3],
                DJI[3],
                VIX[3],
                NDX[3],
                GOLD[3]
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FundamentalStockObject that)) return false;
        return Objects.equals(createTime, that.createTime) && Objects.deepEquals(SPX, that.SPX) && Objects.deepEquals(DXY, that.DXY) && Objects.deepEquals(DJI, that.DJI) && Objects.deepEquals(VIX, that.VIX) && Objects.deepEquals(NDX, that.NDX) && Objects.deepEquals(GOLD, that.GOLD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(createTime, Arrays.hashCode(SPX), Arrays.hashCode(DXY), Arrays.hashCode(DJI), Arrays.hashCode(VIX), Arrays.hashCode(NDX), Arrays.hashCode(GOLD));
    }
}
