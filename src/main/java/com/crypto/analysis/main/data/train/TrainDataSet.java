package com.crypto.analysis.main.data.train;

import com.crypto.analysis.main.data.DataNormalizer;
import com.crypto.analysis.main.data.DataTransformer;
import com.crypto.analysis.main.enumerations.DataLength;
import com.crypto.analysis.main.enumerations.TimeFrame;
import com.crypto.analysis.main.vo.DataObject;
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
    private DataNormalizer normalizer;


    private TrainDataSet(String symbol, DataLength dl) {
        this.symbol = symbol;
        this.dl = dl;
    }


    public static TrainDataSet prepareTrainSet(String symbol, DataLength dl) {
        System.out.println("Preparing train set..");
        TrainDataSet trainDataSet = new TrainDataSet(symbol, dl);

        //TrainDataCSV trainDataCSV = new TrainDataCSV(symbol, TimeFrame.ONE_DAY, dl);
        TrainDataBinance trainDataBinance = new TrainDataBinance(symbol, TimeFrame.ONE_DAY, dl);

        LinkedList<DataObject[]> data = new LinkedList<>();
      //  data.addAll(trainDataCSV.getData());
        data.addAll(trainDataBinance.getData());

        DataTransformer transformer = new DataTransformer(data, dl);
        transformer.transform();
        LinkedList<double[][]> trainData = transformer.getTrainData();
        LinkedList<double[][]> trainResult = transformer.getTrainResult();

        LinkedList<double[][]> testData = new LinkedList<>();
        LinkedList<double[][]> testResult = new LinkedList<>();
        int count = trainData.size();
        for (int i = count - trainData.size()/6; i < trainData.size(); i++) {
            testData.add(trainData.remove(i));
            testResult.add(trainResult.remove(i));
        }

        trainDataSet.setTrainData(trainData);
        trainDataSet.setTrainResult(trainResult);
        trainDataSet.setTestData(testData);
        trainDataSet.setTestResult(testResult);
        trainDataSet.setNormalizer(transformer.getNormalizer());

        System.out.println("Train data set size: " + trainData.size());
        System.out.println("Train data set result size: " + trainResult.size());
        System.out.println("Test data set size: " + testData.size());
        System.out.println("Test data set result size: " + testResult.size());
        return trainDataSet;
    }

}
