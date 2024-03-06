package com.crypto.analysis.main.data.train;

import com.crypto.analysis.main.enumerations.DataLength;
import com.crypto.analysis.main.enumerations.TimeFrame;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;

@Getter
@Setter
public class TrainDataSet {
    private final String symbol;
    private final DataLength dl;
    private LinkedList<double[][]> trainData = new LinkedList<>();
    private LinkedList<double[][]> trainResult = new LinkedList<>();

    private LinkedList<double[][]> testData = new LinkedList<>();
    private LinkedList<double[][]> testResult = new LinkedList<>();


    public TrainDataSet(String symbol, DataLength dl) {
        this.symbol = symbol;
        this.dl = dl;
    }

    public static TrainDataSet prepareTrainSet(String symbol, DataLength dl) {
        System.out.println("Preparing train set..");
        TrainDataSet trainDataSet = new TrainDataSet(symbol, dl);

        LinkedList<double[][]> lTrainData = new LinkedList<>();
        LinkedList<double[][]> lTrainResult = new LinkedList<>();

        LinkedList<double[][]> lTestData = new LinkedList<>();
        LinkedList<double[][]> lTestResult = new LinkedList<>();

        /*
        TrainDataCSV trainDataCSV = new TrainDataCSV(symbol, TimeFrame.ONE_DAY, 20, 1);

        trainData.addAll(trainDataCSV.getTrainData());
        trainResult.addAll(trainDataCSV.getTrainResult());
        */
        TrainDataBinance train = new TrainDataBinance(symbol, TimeFrame.ONE_HOUR, dl);

        lTrainData.addAll(train.getTrainData());
        lTrainResult.addAll(train.getTrainResult());

        lTestData.addAll(train.getTestData());
        lTestResult.addAll(train.getTestResult());

        trainDataSet.setTrainData(lTrainData);
        trainDataSet.setTrainResult(lTrainResult);

        trainDataSet.setTestData(lTestData);
        trainDataSet.setTestResult(lTestResult);

        System.out.println("Train set size: " + lTrainData.size());
        System.out.println("Test set size: " + lTrainResult.size());
        System.out.println("Train set result size: " + lTestData.size());
        System.out.println("Test set result size: " + lTestResult.size());

        return trainDataSet;
    }

}
