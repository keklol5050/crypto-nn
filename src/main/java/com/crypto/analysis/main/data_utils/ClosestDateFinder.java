package com.crypto.analysis.main.data_utils;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;


public class ClosestDateFinder {

    public static Double findClosestValue(TreeMap<Date, Double> sortedMap, Date targetDate) {
        Date previousDate = null;

        for (Map.Entry<Date, Double> entry : sortedMap.entrySet()) {
            if (entry.getKey().before(targetDate)) {
                previousDate = entry.getKey();
            }
        }

        return sortedMap.get(previousDate);
    }
}