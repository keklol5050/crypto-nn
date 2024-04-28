package com.crypto.analysis.main.core.volatility;

import com.crypto.analysis.main.core.data_utils.normalizers.Transposer;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.train.TrainDataBinance;
import com.crypto.analysis.main.core.data_utils.train.TrainDataCSV;
import com.crypto.analysis.main.core.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.core.model.DataVisualisation;
import com.crypto.analysis.main.core.ndata.CSVCoinDataSet;
import com.crypto.analysis.main.core.vo.DataObject;
import lombok.Getter;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.jfree.data.xy.XYSeries;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Collections;
import java.util.LinkedList;

import static com.crypto.analysis.main.core.data_utils.select.StaticData.BATCH_SIZE;

@Getter
public class VolatilityTrainSet {
    private final Coin coin;
    private ListDataSetIterator<DataSet> trainIterator;
    private ListDataSetIterator<DataSet> testIterator;
    private INDArray labelsMask;
    private int countInput;
    private int countOutput;
    private int sequenceLength;

    private VolatilityTrainSet(Coin coin) {
        this.coin = coin;
    }

    public static VolatilityTrainSet prepareTrainSet(Coin coin, CSVCoinDataSet set) {
        if (set.getInterval() != TimeFrame.FOUR_HOUR) throw new IllegalArgumentException("Time frame for volatility regression must be 4h!");

        System.out.println("Preparing train set..");
        VolatilityTrainSet trainDataSet = new VolatilityTrainSet(coin);

        TrainDataCSV trainData = new TrainDataCSV(coin, set.getInterval(), DataLength.VOLATILITY_REGRESSION, set);

        LinkedList<DataObject[]> data = new LinkedList<>(trainData.getData());

        return getTrainDataSet(trainDataSet, data);
    }

    public static VolatilityTrainSet prepareTrainSet(Coin coin, FundamentalDataUtil fdUtil) {
        System.out.println("Preparing train set..");
        VolatilityTrainSet trainDataSet = new VolatilityTrainSet(coin);

        TrainDataBinance trainDataBinance = new TrainDataBinance(coin, TimeFrame.FOUR_HOUR, DataLength.VOLATILITY_REGRESSION, fdUtil);

        LinkedList<DataObject[]> data = new LinkedList<>(trainDataBinance.getData());

        return getTrainDataSet(trainDataSet, data);
    }

    public static VolatilityTrainSet getTrainDataSet(VolatilityTrainSet trainDataSet, LinkedList<DataObject[]> data) {
        int countInput = DataLength.VOLATILITY_REGRESSION.getCountInput();
        int countOutput = DataLength.VOLATILITY_REGRESSION.getCountOutput();

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

        for (double[][] in : dataArr) {
            if (countInput + countOutput != in.length)
                throw new ArithmeticException("Parameters count are not equals");
            if (countOutput < 1 || countInput < 1)
                throw new IllegalArgumentException("Parameters cannot be zero or negative");

            double[][] diff = new double[in.length][];
            for (int i = 0; i < in.length; i++) {
                diff[i] = new double[6];
                diff[i][0] = in[i][1] - in[i][3];
                diff[i][1] = in[i][2] - in[i][3];
                diff[i][2] = in[i][4];
                diff[i][3] = in[i][37];
                diff[i][4] = in[i][42];
                diff[i][5] = in[i][43];
            }
            double[][] input = new double[countInput][];
            System.arraycopy(diff, 0, input, 0, input.length);

            double[][] output = new double[countOutput][];
            System.arraycopy(diff, input.length, output, 0, output.length);

            double[][] finalOutput = new double[countOutput][];
            for (int i = 0; i < finalOutput.length; i++) {
                finalOutput[i] = new double[2];
                System.arraycopy(output[i], 0, finalOutput[i], 0, finalOutput[i].length);
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
        CSVCoinDataSet cs = new CSVCoinDataSet(Coin.BTCUSDT, TimeFrame.FOUR_HOUR);
        cs.load();
        VolatilityTrainSet volatilityTrainSet = VolatilityTrainSet.prepareTrainSet(Coin.BTCUSDT, cs);
        System.out.println(volatilityTrainSet.getTrainIterator().next().getFeatures());
        System.out.println(volatilityTrainSet.getTestIterator().next().getFeatures());
        System.out.println(volatilityTrainSet.getTrainIterator().next().getLabels());
        System.out.println(volatilityTrainSet.getTestIterator().next().getLabels());
        System.out.println(volatilityTrainSet.getLabelsMask());
        System.out.println(volatilityTrainSet.getCountInput());
        System.out.println(volatilityTrainSet.getCountOutput());
        System.out.println(volatilityTrainSet.getSequenceLength());
        DataSet set = volatilityTrainSet.getTrainIterator().next(1);
        double[][] values = set.getFeatures().slice(0).toDoubleMatrix();
        XYSeries high = new XYSeries("High");
        XYSeries low = new XYSeries("Low");
        for (int i = 0; i < values[0].length; i++) {
            high.add(i, values[0][i]);
            low.add(i, values[1][i]);
        }
        DataVisualisation.visualize("HL", "count", "price change", high, low);
    }

}