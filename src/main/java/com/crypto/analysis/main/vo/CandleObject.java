package com.crypto.analysis.main.vo;

import lombok.Getter;

import java.util.Date;

@Getter
public class CandleObject { // формат свічки графіку
    private final Date openTime;
    private final double open;
    private final double high;
    private final double low;
    private final double close;
    private final double volume;
    private final Date closeTime;


    public CandleObject(Date openTime, double open, double high, double low, double close, double volume, Date closeTime) {
        this.openTime = openTime;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.closeTime = closeTime;
    }

    @Override
    public String toString() {
        return "\nCandleObject{" +
                "\nopenTime=" + openTime +
                ",\n open=" + open +
                ",\n high=" + high +
                ",\n low=" + low +
                ",\n close=" + close +
                ",\n closeTime=" + closeTime +
                "\n}";
    }

    public double[] getValuesArr() {
        return new double[]{
                open,
                high,
                low,
                close
        };
    }
}