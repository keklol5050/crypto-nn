package com.crypto.analysis.main.core.vo.indication;

import lombok.Getter;

import java.util.Date;
import java.util.TreeMap;

public class ETHBTCPriceObject {
    @Getter
    private final TreeMap<Date, float[]> map;

    public ETHBTCPriceObject(TreeMap<Date, float[]> map) {
        this.map = map;
    }

    public float getValueForNearestDate(Date targetDate) {
        return map.get(targetDate)[3];
    }

    public float getFirst() {
        return map.firstEntry().getValue()[3];
    }

    public boolean contains(Date date) {
        return map.containsKey(date);
    }
}
