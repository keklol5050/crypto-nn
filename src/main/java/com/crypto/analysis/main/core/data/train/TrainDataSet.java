package com.crypto.analysis.main.core.data.train;

import com.crypto.analysis.main.core.data.refactor.DataTransformer;
import com.crypto.analysis.main.core.data_utils.normalizers.robust.RobustScaler;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.core.ndata.CSVCoinDataSet;
import com.crypto.analysis.main.core.vo.DataObject;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.vo.TrainSetElement;
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
    private RobustScaler normalizer;


    private TrainDataSet(Coin coin, DataLength dl) {
        this.coin = coin;
        this.dl = dl;
    }


    public static TrainDataSet prepareTrainSet(Coin coin, DataLength dl, CSVCoinDataSet set) {
        System.out.println("Preparing train set..");
        TrainDataSet trainDataSet = new TrainDataSet(coin, dl);

        TrainDataCSV trainData = new TrainDataCSV(coin, set.getInterval(), dl, set);

        LinkedList<DataObject[]> data = new LinkedList<>(trainData.getData());

        return getTrainDataSet(dl, trainDataSet, data);
    }

    public static TrainDataSet prepareTrainSet(Coin coin, DataLength dl, TimeFrame interval, FundamentalDataUtil fdUtil) {
        System.out.println("Preparing train set..");
        TrainDataSet trainDataSet = new TrainDataSet(coin, dl);

        TrainDataBinance trainDataBinance = new TrainDataBinance(coin, interval, dl, fdUtil);

        LinkedList<DataObject[]> data = new LinkedList<>(trainDataBinance.getData());

        return getTrainDataSet(dl, trainDataSet, data);
    }

    @NotNull
    public static TrainDataSet getTrainDataSet(DataLength dl, TrainDataSet trainDataSet, LinkedList<DataObject[]> data) {
        DataTransformer transformer = new DataTransformer(data, dl);
        transformer.transform();

        LinkedList<TrainSetElement> dataSet = transformer.getTrainData();

        int count = dataSet.size();
        int max = data.size() > 5000 ? count-350 : count-100;

        LinkedList<TrainSetElement> testSet = new LinkedList<TrainSetElement>();
        for (int i = max; i < count; i++) {
           testSet.add(0, dataSet.removeLast());
        }

        Collections.shuffle(dataSet);
        Collections.shuffle(dataSet);

        LinkedList<double[][]> trainData = new LinkedList<>();
        LinkedList<double[][]> trainResult = new LinkedList<>();

        LinkedList<double[][]> testData = new LinkedList<>();
        LinkedList<double[][]> testResult = new LinkedList<>();

        for (TrainSetElement trainSetElement : dataSet) {
            trainData.add(trainSetElement.getData());
            trainResult.add(trainSetElement.getResult());
        }

        for (TrainSetElement trainSetElement : testSet) {
            testData.add(trainSetElement.getData());
            testResult.add(trainSetElement.getResult());
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

    public static void main(String[] args) {
        CSVCoinDataSet cs = new CSVCoinDataSet(Coin.BTCUSDT, TimeFrame.ONE_HOUR);
        cs.load();
        TrainDataSet trainDataSet =  TrainDataSet.prepareTrainSet(Coin.BTCUSDT, DataLength.S30_3, cs);
    }

}
