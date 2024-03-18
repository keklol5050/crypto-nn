package com.crypto.analysis.main.vo;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class SentimentHistoryObject {
    private final TreeMap<Date, double[]> map;

    public SentimentHistoryObject(TreeMap<Date, double[]> map) {
        this.map = map;
    }

    public double[] getValueForNearestDate(Date targetDate) {
        Map.Entry<Date, double[]> entry = map.floorEntry(targetDate);
        return entry.getValue();
    }

    public double[] getFirst() {
        return map.firstEntry().getValue();
    }

}
