package com.crypto.analysis.main.vo.indication;

import java.util.Date;
import java.util.TreeMap;

public class OpenInterestHistoryObject {
    private final TreeMap<Date, Double> map;
    public OpenInterestHistoryObject(TreeMap<Date, Double> map) {
        this.map = map;
    }
    public double getValueForNearestDate(Date targetDate) {
        return map.floorEntry(targetDate).getValue();
    }
    public double getFirst() {
        return map.firstEntry().getValue();
    }

    public boolean contains(Date date) {
        return map.containsKey(date);
    }
}
