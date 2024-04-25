package com.crypto.analysis.main.core.data_utils.train;

import com.crypto.analysis.main.core.data_utils.normalizers.Transposer;
import com.crypto.analysis.main.core.data_utils.normalizers.Differentiator;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.core.ndata.CSVCoinDataSet;
import com.crypto.analysis.main.core.vo.DataObject;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import lombok.Getter;
import lombok.Setter;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.jetbrains.annotations.NotNull;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Collections;
import java.util.LinkedList;

import static com.crypto.analysis.main.core.data_utils.normalizers.Differentiator.refactor;
import static com.crypto.analysis.main.core.data_utils.select.StaticData.*;

@Getter
@Setter
public class TrainDataSet {
    private final Coin coin;
    private final DataLength dl;
    private ListDataSetIterator<DataSet> trainIterator;
    private ListDataSetIterator<DataSet> testIterator;
    private INDArray labelsMask;
    private int countInput;
    private int countOutput;
    private int sequenceLength;

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
        int countInput = dl.getCountInput()-NUMBER_OF_DIFFERENTIATIONS;
        int countOutput = dl.getCountOutput();

        LinkedList<double[][]> dataArr = new LinkedList<double[][]>();
        for (DataObject[] datum : data) {
            double[][] doArray = new double[datum.length][];
            for (int j = 0; j < datum.length; j++) {
                doArray[j] = datum[j].getParamArray();
            }
            dataArr.add(doArray);
        }

        INDArray labelsMask = Nd4j.zeros(1, countInput);
        for (int i = 0; i < countOutput; i++) {
            labelsMask.putScalar(new int[]{0, i}, 1.0);
        }

        LinkedList<DataSet> trainSets = new LinkedList<DataSet>();

        Differentiator differentiator = new Differentiator();
        for (double[][] in : dataArr) {
            if (countInput + countOutput != (in.length - NUMBER_OF_DIFFERENTIATIONS))
                throw new ArithmeticException("Parameters count are not equals");
            if (countOutput < 1 || countInput < 1)
                throw new IllegalArgumentException("Parameters cannot be zero or negative");

            double[][] diff = refactor(in, differentiator, false);

            double[][] input = new double[countInput][];
            System.arraycopy(diff, 0, input, 0, input.length);

            double[][] output = new double[countOutput][];
            System.arraycopy(diff, input.length, output, 0, output.length);

            double[][] finalOutput = new double[countOutput][];
            for (int i = 0; i < finalOutput.length; i++) {
                finalOutput[i] = new double[MASK_OUTPUT.length];
                for (int j = 0; j < finalOutput[i].length; j++) {
                    finalOutput[i][j] = output[i][MASK_OUTPUT[j]];
                }
            }
            input = Transposer.transpose(input);
            finalOutput = Transposer.transpose(finalOutput, input[0].length);

            INDArray inputArr = Nd4j.createFromArray(new double[][][]{input});
            INDArray outputArr = Nd4j.createFromArray(new double[][][]{finalOutput});

            trainSets.add(new DataSet(inputArr, outputArr, null, labelsMask));
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
        trainDataSet.sequenceLength = countInput;

        System.out.println("Train set size: " + trainSets.size());
        System.out.println("Test set size: " + testSets.size());
        return trainDataSet;
    }

    public static void main(String[] args) {
        CSVCoinDataSet cs = new CSVCoinDataSet(Coin.BTCUSDT, TimeFrame.ONE_HOUR);
        cs.load();
        TrainDataSet trainDataSet =  TrainDataSet.prepareTrainSet(Coin.BTCUSDT, DataLength.S30_3, cs);
    }

}
