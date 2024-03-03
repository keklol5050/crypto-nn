package com.crypto.analysis.main.neural.train;


import com.crypto.analysis.main.enumerations.TimeFrame;
import lombok.Getter;

import java.util.LinkedList;

@Getter
public class TrainDataSet {
    private final String symbol;
    private final LinkedList<double[][]> trainData = new LinkedList<>();
    private final LinkedList<double[]> trainResult = new LinkedList<>();

    private final LinkedList<double[][]> testData = new LinkedList<>();
    private final LinkedList<double[]> testResult = new LinkedList<>();


    public TrainDataSet(String symbol) {
        this.symbol = symbol;
    }

    public void prepareTrainSet() {
        if (!trainResult.isEmpty() || !trainData.isEmpty()) throw new IllegalArgumentException();
        System.out.println("Preparing train set..");

        TrainDataCSV trainDataCSV = new TrainDataCSV(symbol, TimeFrame.ONE_DAY);

        trainData.addAll(trainDataCSV.getTrainData());
        trainResult.addAll(trainDataCSV.getTrainResult());

        TrainDataBinance train = new TrainDataBinance(symbol, TimeFrame.ONE_DAY);

        testData.addAll(train.getTrainData());
        testResult.addAll(train.getTrainResult());

        testData.addAll(train.getTestData());
        testResult.addAll(train.getTestResult());

        System.out.println("Train set size: " + trainData.size());
        System.out.println("Test set size: " + testData.size());
        System.out.println("Train set result size: " + trainResult.size());
        System.out.println("Test set result size: " + testResult.size());
    }

}
