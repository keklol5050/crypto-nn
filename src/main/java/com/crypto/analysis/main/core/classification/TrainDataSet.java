package com.crypto.analysis.main.core.classification;

import com.crypto.analysis.main.core.data.refactor.Transposer;
import com.crypto.analysis.main.core.data.train.TrainDataBinance;
import com.crypto.analysis.main.core.data.train.TrainDataCSV;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.core.ndata.CSVCoinDataSet;
import com.crypto.analysis.main.core.vo.DataObject;
import lombok.Getter;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import java.util.*;

import static com.crypto.analysis.main.core.data_utils.select.StaticData.BATCH_SIZE;

@Getter
public class TrainDataSet {
    private final Coin coin;
    private ListDataSetIterator<DataSet> trainIterator;
    private ListDataSetIterator<DataSet> testIterator;
    private INDArray labelsMask;
    private int countInput;
    private int countOutput;

    private TrainDataSet(Coin coin) {
        this.coin = coin;
    }

    public static TrainDataSet prepareTrainSet(Coin coin, CSVCoinDataSet set) {
        System.out.println("Preparing train set..");
        TrainDataSet trainDataSet = new TrainDataSet(coin);

        TrainDataCSV trainData = new TrainDataCSV(coin, set.getInterval(), DataLength.CLASSIFICATION, set);

        LinkedList<DataObject[]> data = new LinkedList<>(trainData.getData());

        return getTrainDataSet(trainDataSet, data, set.getInterval());
    }

    public static TrainDataSet prepareTrainSet(Coin coin, TimeFrame interval, FundamentalDataUtil fdUtil) {
        System.out.println("Preparing train set..");
        TrainDataSet trainDataSet = new TrainDataSet(coin);

        TrainDataBinance trainDataBinance = new TrainDataBinance(coin, interval, DataLength.CLASSIFICATION, fdUtil);

        LinkedList<DataObject[]> data = new LinkedList<>(trainDataBinance.getData());

        return getTrainDataSet(trainDataSet, data, interval);
    }

    private static TrainDataSet getTrainDataSet(TrainDataSet trainDataSet, LinkedList<DataObject[]> data, TimeFrame interval) {
        LinkedHashMap<double[][], double[]> indicatorsData = new LinkedHashMap<>();
        double changeIndex = PriceMovingTypes.getTimeFrameChangePercentage(interval);

        int countInput = DataLength.CLASSIFICATION.getCountInput();
        int countOutput = DataLength.CLASSIFICATION.getCountOutput();

        INDArray labelsMask = Nd4j.zeros(1, countInput);
        labelsMask.putScalar(new int[]{0, countInput-1}, 1.0);

        for (DataObject[] datum : data) {
            if (datum.length != countOutput + countInput)
                throw new IllegalArgumentException("Wrong data array length: " + datum.length);

            double[][] doArray = new double[countInput][];
            double[] closePriceArray = new double[countOutput + 1];

            for (int j = 0; j < countInput; j++) {
                doArray[j] = datum[j].getCurrentIndicators().getIndicatorValues();
            }

            for (int j = countInput - 1; j < datum.length; j++) {
                closePriceArray[j - (countInput - 1)] = datum[j].getCandle().getClose();
            }

            indicatorsData.put(doArray, closePriceArray);
        }

        LinkedList<DataSet> trainSets = new LinkedList<DataSet>();

        for (Map.Entry<double[][], double[]> entry : indicatorsData.entrySet()) {
            double[][] inputTransposed = Transposer.transpose(entry.getKey());
            INDArray input = Nd4j.createFromArray(new double[][][]{inputTransposed});

            double[] diff = new double[entry.getValue().length - 1];
            for (int i = 1; i < entry.getValue().length; i++) {
                diff[i - 1] = ((entry.getValue()[i] - entry.getValue()[i - 1]) / (entry.getValue()[i - 1])) * 100;
            }
            double diffPercentage = Arrays.stream(diff).sum();

            double[][] labels = new double[PriceMovingTypes.countClasses][countInput];

            if (diffPercentage >= changeIndex)
                labels[PriceMovingTypes.HIGH_INCREASE.ordinal()][countInput- 1] = 1;

            else if (diffPercentage >= changeIndex / 2)
                labels[PriceMovingTypes.INCREASE.ordinal()][countInput - 1] = 1;

            else if (diffPercentage <= -changeIndex)
                labels[PriceMovingTypes.HIGH_DECREASE.ordinal()][countInput - 1] = 1;

            else if (diffPercentage <= -changeIndex / 2)
                labels[PriceMovingTypes.DECREASE.ordinal()][countInput - 1] = 1;

            else labels[PriceMovingTypes.NEUTRAL.ordinal()][countInput - 1] = 1;


            INDArray output = Nd4j.createFromArray(new double[][][]{labels});

            DataSet set = new DataSet(input, output, null, labelsMask);
            trainSets.add(set);
        }

        LinkedList<DataSet> testSets = new LinkedList<DataSet>();

        int max = data.size() > 5000 ? 550 : 100;

        for (int i = 0; i < max; i++) {
            testSets.add(0, trainSets.removeLast());
        }

        Collections.shuffle(trainSets);
        Collections.shuffle(trainSets);

        ListDataSetIterator<DataSet> trainIterator = new ListDataSetIterator<>(trainSets, BATCH_SIZE);
        ListDataSetIterator<DataSet> testIterator = new ListDataSetIterator<>(testSets, BATCH_SIZE);

        trainDataSet.trainIterator = trainIterator;
        trainDataSet.testIterator = testIterator;
        trainDataSet.labelsMask = labelsMask;
        trainDataSet.countInput = trainSets.get(0).numInputs();
        trainDataSet.countOutput = trainSets.get(0).numOutcomes();

        System.out.println("Train set size: " + trainSets.size());
        System.out.println("Test set size: " + testSets.size());

        return trainDataSet;
    }

    public static void main(String[] args) {
        CSVCoinDataSet cs = new CSVCoinDataSet(Coin.BTCUSDT, TimeFrame.ONE_HOUR);
        cs.load();
        TrainDataSet trainDataSet = prepareTrainSet(Coin.BTCUSDT, cs);
    }
}
