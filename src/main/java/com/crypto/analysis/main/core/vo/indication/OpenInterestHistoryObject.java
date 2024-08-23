package com.crypto.analysis.main.core.vo.indication;

import lombok.Getter;

import java.util.Date;
import java.util.TreeMap;

public class OpenInterestHistoryObject {
    @Getter
    private final TreeMap<Date, Float> map;

    public OpenInterestHistoryObject(TreeMap<Date, Float> map) {
        this.map = map;
    }

    public float getValueForNearestDate(Date targetDate) {
        return map.floorEntry(targetDate).getValue();
    }

    public float getFirst() {
        return map.firstEntry().getValue();
    }

    public boolean contains(Date date) {
        return map.containsKey(date);
    }
}
