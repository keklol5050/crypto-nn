package com.crypto.analysis.main.core.vo.indication;

import lombok.Getter;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class FundingHistoryObject {
    @Getter
    private final TreeMap<Date, Float> map;

    public FundingHistoryObject(TreeMap<Date, Float> map) {
        this.map = map;
    }

    public float getValueForNearestDate(Date targetDate) {
        Map.Entry<Date, Float> entry = map.floorEntry(targetDate);
        return entry.getValue();
    }

    public float getFirst() {
        return map.firstEntry().getValue();
    }
}
