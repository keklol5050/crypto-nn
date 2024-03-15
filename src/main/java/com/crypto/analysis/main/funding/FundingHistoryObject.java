package com.crypto.analysis.main.funding;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class FundingHistoryObject {
    private final TreeMap<Date, Double> map;
    public FundingHistoryObject(TreeMap<Date, Double> map) {
        this.map = map;
    }
    public double getValueForNearestDate(Date targetDate) {
        Map.Entry<Date, Double> entry = map.floorEntry(targetDate);
        return entry.getValue();
    }
    public double getFirst() {
        return map.firstEntry().getValue();
    }
    public static void main(String[] args) {
        INDArray array = Nd4j.create(new float[][]{
                {31f, 0f, 0f},
                {5f, 0f, 0f},
                {42.4f, 0f, 0f},
                {45.4f, 0f, 0f},
                {42.4f, 0f, 0f}
        });

        double columnIndices = array.argMax(0).getDouble(0);
        System.out.println(columnIndices);
    }
}
