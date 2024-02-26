package com.crypto.analysis.main.data_utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

import static com.crypto.analysis.main.data_utils.BinanceDataUtil.client;

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