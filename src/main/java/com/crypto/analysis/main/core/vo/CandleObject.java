package com.crypto.analysis.main.core.vo;

import lombok.Getter;

import java.util.Date;
import java.util.Objects;

@Getter
public class CandleObject { // формат свічки графіку
    private final Date openTime;
    private final float open;
    private final float high;
    private final float low;
    private final float close;
    private final float volume;
    private final Date closeTime;


    public CandleObject(Date openTime, float open, float high, float low, float close, float volume, Date closeTime) {
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
                ",\n volume=" + volume +
                ",\n closeTime=" + closeTime +
                "\n}";
    }

    public float[] getValuesArr() {
        return new float[]{
                open,
                high,
                low,
                close,
                volume
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CandleObject that)) return false;
        return Double.compare(getOpen(), that.getOpen()) == 0 && Double.compare(getHigh(), that.getHigh()) == 0 && Double.compare(getLow(), that.getLow()) == 0 && Double.compare(getClose(), that.getClose()) == 0 && Double.compare(getVolume(), that.getVolume()) == 0 && Objects.equals(getOpenTime(), that.getOpenTime()) && Objects.equals(getCloseTime(), that.getCloseTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpenTime(), getOpen(), getHigh(), getLow(), getClose(), getVolume(), getCloseTime());
    }
}