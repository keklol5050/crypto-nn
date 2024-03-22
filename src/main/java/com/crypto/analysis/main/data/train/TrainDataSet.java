package com.crypto.analysis.main.data.train;

import com.crypto.analysis.main.data.refactor.DataTransformer;
import com.crypto.analysis.main.data_utils.normalizers.BatchNormalizer;
import com.crypto.analysis.main.data_utils.select.coin.Coin;
import com.crypto.analysis.main.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.fundamental.stock.FundamentalDataUtil;
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
    private BatchNormalizer normalizer;


    private TrainDataSet(Coin coin, DataLength dl) {
        this.coin = coin;
        this.dl = dl;
    }


    public static TrainDataSet prepareTrainSet(Coin coin, DataLength dl, CSVCoinDataSet set15m, CSVCoinDataSet set1h, CSVCoinDataSet set4h) {
        System.out.println("Preparing train set..");
        TrainDataSet trainDataSet = new TrainDataSet(coin, dl);

        LinkedList<DataObject[]> data = new LinkedList<>();

        TrainDataCSV trainDataCSV15m = new TrainDataCSV(coin, TimeFrame.FIFTEEN_MINUTES, dl, set15m);
        TrainDataCSV trainDataCSV1h = new TrainDataCSV(coin, TimeFrame.ONE_HOUR, dl, set1h);
        TrainDataCSV trainDataCSV4h = new TrainDataCSV(coin, TimeFrame.FOUR_HOUR, dl, set4h);

        data.addAll(trainDataCSV15m.getData());
        data.addAll(trainDataCSV1h.getData());
        data.addAll(trainDataCSV4h.getData());

        return getTrainDataSet(dl, trainDataSet, data);
    }
    public static TrainDataSet prepareTrainSet(Coin coin, DataLength dl, FundamentalDataUtil fdUtil) {
        System.out.println("Preparing train set..");
        TrainDataSet trainDataSet = new TrainDataSet(coin, dl);

        LinkedList<DataObject[]> data = new LinkedList<>();

        TrainDataBinance trainDataBinance15m = new TrainDataBinance(coin, TimeFrame.FIFTEEN_MINUTES, dl, fdUtil);
        TrainDataBinance trainDataBinance1h = new TrainDataBinance(coin, TimeFrame.ONE_HOUR, dl, fdUtil);
        TrainDataBinance trainDataBinance4h = new TrainDataBinance(coin, TimeFrame.FOUR_HOUR, dl, fdUtil);

        data.addAll(trainDataBinance15m.getData());
        data.addAll(trainDataBinance1h.getData());
        data.addAll(trainDataBinance4h.getData());

        return getTrainDataSet(dl, trainDataSet, data);
    }

    @NotNull
    public static TrainDataSet getTrainDataSet(DataLength dl, TrainDataSet trainDataSet, LinkedList<DataObject[]> data) {
        DataTransformer transformer = new DataTransformer(data, dl);
        transformer.transform();

        LinkedList<TrainSetElement> dataSet = transformer.getTrainData();
        Collections.shuffle(dataSet);
        Collections.shuffle(dataSet);

        LinkedList<double[][]> trainData =  new LinkedList<>();
        LinkedList<double[][]> trainResult =  new LinkedList<>();

        LinkedList<double[][]> testData = new LinkedList<>();
        LinkedList<double[][]> testResult = new LinkedList<>();
        int count = dataSet.size();
        int max = data.size() > 5000 ? count-700 : count-70;

        for (int i = 0; i < count; i++) {
            if (i < max) {
                trainData.add(dataSet.get(i).getData());
                trainResult.add(dataSet.get(i).getResult());
            } else {
                testData.add(dataSet.get(i).getData());
                testResult.add(dataSet.get(i).getResult());
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
        System.out.println();
        return trainDataSet;
    }
}
