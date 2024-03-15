package com.crypto.analysis.main.data.train;

import com.crypto.analysis.main.data.DataNormalizer;
import com.crypto.analysis.main.data.DataTransformer;
import com.crypto.analysis.main.enumerations.Coin;
import com.crypto.analysis.main.enumerations.DataLength;
import com.crypto.analysis.main.enumerations.TimeFrame;
import com.crypto.analysis.main.ndata.CSVCoinDataSet;
import com.crypto.analysis.main.vo.DataObject;
import com.crypto.analysis.main.vo.TrainSetElement;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedList;

@Getter
@Setter
public class TrainDataSet {
    private final Coin coin;
    private final DataLength dl;
    private LinkedList<double[][]> trainData = new LinkedList<>();
    private LinkedList<double[][]> trainResult = new LinkedList<>();

    private LinkedList<double[][]> testData = new LinkedList<>();
    private LinkedList<double[][]> testResult = new LinkedList<>();
    private DataNormalizer normalizer;


    private TrainDataSet(Coin coin, DataLength dl) {
        this.coin = coin;
        this.dl = dl;
    }


    public static TrainDataSet prepareTrainSet(Coin coin, DataLength dl, CSVCoinDataSet set) {
        System.out.println("Preparing train set..");
        TrainDataSet trainDataSet = new TrainDataSet(coin, dl);

        TrainDataCSV trainDataCSV = new TrainDataCSV(coin, TimeFrame.FIFTEEN_MINUTES, dl, set);
        TrainDataBinance trainDataBinance = new TrainDataBinance(coin, TimeFrame.FIFTEEN_MINUTES, dl);

        LinkedList<DataObject[]> data = new LinkedList<>();
        data.addAll(trainDataCSV.getData());
        data.addAll(trainDataBinance.getData());

        return getTrainDataSet(dl, trainDataSet, data);
    }
    public static TrainDataSet prepareTrainSet(Coin coin, DataLength dl) {
        System.out.println("Preparing train set..");
        TrainDataSet trainDataSet = new TrainDataSet(coin, dl);

        TrainDataBinance trainDataBinance = new TrainDataBinance(coin, TimeFrame.FIFTEEN_MINUTES, dl);

        LinkedList<DataObject[]> data = new LinkedList<>(trainDataBinance.getData());

        return getTrainDataSet(dl, trainDataSet, data);
    }

    @NotNull
    public static TrainDataSet getTrainDataSet(DataLength dl, TrainDataSet trainDataSet, LinkedList<DataObject[]> data) {
        DataTransformer transformer = new DataTransformer(data, dl);
        transformer.transform();

        LinkedList<TrainSetElement> dataSet = transformer.getTrainData();
        Collections.shuffle(dataSet);

        LinkedList<double[][]> trainData =  new LinkedList<>();
        LinkedList<double[][]> trainResult =  new LinkedList<>();

        LinkedList<double[][]> testData = new LinkedList<>();
        LinkedList<double[][]> testResult = new LinkedList<>();
        int count = dataSet.size();
        int max = data.size() > 500 ? count-count/12 : count-count/6;

        for (int i = 0; i < count; i++) {
            if (i < max) {
                trainData.add(dataSet.get(i).getDataMatrix());
                trainResult.add(dataSet.get(i).getResultMatrix());
            } else {
                testData.add(dataSet.get(i).getDataMatrix());
                testResult.add(dataSet.get(i).getResultMatrix());
            }
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
