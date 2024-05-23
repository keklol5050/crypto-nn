package com.crypto.analysis.main.core.vo.indication;

import lombok.Getter;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class SentimentHistoryObject {
    @Getter
    private final TreeMap<Date, float[]> map;

    public SentimentHistoryObject(TreeMap<Date, float[]> map) {
        this.map = map;
    }

    public float[] getValueForNearestDate(Date targetDate) {
        Map.Entry<Date, float[]> entry = map.floorEntry(targetDate);
        return entry.getValue();
    }

    public float[] getFirst() {
        return map.firstEntry().getValue();
    }

}
