package com.crypto.analysis.main.core.regression;

import com.crypto.analysis.main.core.data_utils.normalizers.Differentiator;
import com.crypto.analysis.main.core.data_utils.normalizers.StandardizeNormalizer;
import com.crypto.analysis.main.core.data_utils.normalizers.Transposer;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.train.TrainDataBinance;
import com.crypto.analysis.main.core.data_utils.train.TrainDataCSV;
import com.crypto.analysis.main.core.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.core.data_utils.utils.mutils.DataVisualisation;
import com.crypto.analysis.main.core.ndata.CSVCoinDataSet;
import com.crypto.analysis.main.core.vo.DataObject;
import lombok.Getter;
import lombok.Setter;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.jetbrains.annotations.NotNull;
import org.jfree.data.xy.XYSeries;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import static com.crypto.analysis.main.core.data_utils.select.StaticData.*;

@Getter
@Setter
public class RegressionDataSet {
    private final Coin coin;
    private final DataLength dl;
    private ListDataSetIterator<DataSet> trainIterator;
    private ListDataSetIterator<DataSet> testIterator;
    private INDArray labelsMask;
    private int countInput;
    private int countOutput;
    private int sequenceLength;

    private RegressionDataSet(Coin coin, DataLength dl) {
        this.coin = coin;
        this.dl = dl;
    }

    public static RegressionDataSet prepareTrainSet(Coin coin, DataLength dl, CSVCoinDataSet set) {
        System.out.println("Preparing train set..");
        RegressionDataSet regressionDataSet = new RegressionDataSet(coin, dl);

        TrainDataCSV trainData = new TrainDataCSV(coin, set.getInterval(), dl, set);

        ArrayList<DataObject[]> data = new ArrayList<>(trainData.getData());

        return getTrainDataSet(dl, regressionDataSet, data);
    }

    public static RegressionDataSet prepareTrainSet(Coin coin, DataLength dl, TimeFrame interval, FundamentalDataUtil fdUtil) {
        System.out.println("Preparing train set..");
        RegressionDataSet regressionDataSet = new RegressionDataSet(coin, dl);

        TrainDataBinance trainDataBinance = new TrainDataBinance(coin, interval, dl, fdUtil);

        ArrayList<DataObject[]> data = new ArrayList<>(trainDataBinance.getData());

        return getTrainDataSet(dl, regressionDataSet, data);
    }

    @NotNull
    public static RegressionDataSet getTrainDataSet(DataLength dl, RegressionDataSet regressionDataSet, ArrayList<DataObject[]> data) {
        int countInput = dl.getCountInput() - NUMBER_OF_DIFFERENTIATIONS;
        int countOutput = dl.getCountOutput();
        int sequenceLength = data.getFirst()[0].getParamArray().length;

        ArrayList<double[][]> dataArr = new ArrayList<double[][]>();
        for (int i = 0; i < data.size(); i++) {
            DataObject[] datum = data.get(i);
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

        ArrayList<DataSet> trainSets = new ArrayList<DataSet>();

        Differentiator differentiator = new Differentiator();
        StandardizeNormalizer normalizer = new StandardizeNormalizer(MASK_OUTPUT, countOutput, sequenceLength);

        for (double[][] in : dataArr) {
            if (countInput + countOutput != (in.length - NUMBER_OF_DIFFERENTIATIONS))
                throw new ArithmeticException("Parameters count are not equals");
            if (countOutput < 1 || countInput < 1)
                throw new IllegalArgumentException("Parameters cannot be zero or negative");

            double[][] diff = refactor(in, differentiator, false, normalizer, countInput);

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

            normalizer.changeBinding(diff, input);
            trainSets.add(new DataSet(inputArr, outputArr, null, labelsMask));
        }
        ArrayList<DataSet> testSets = new ArrayList<DataSet>();

        int max = data.size() > 5000 ? 550 : 100;

        for (int i = 0; i < max; i++) {
            testSets.addFirst(trainSets.removeLast());
        }

        Collections.shuffle(trainSets);
        Collections.shuffle(trainSets);

        ListDataSetIterator<DataSet> trainIterator = new ListDataSetIterator<>(trainSets, BATCH_SIZE);
        ListDataSetIterator<DataSet> testIterator = new ListDataSetIterator<>(testSets, BATCH_SIZE);

        regressionDataSet.trainIterator = trainIterator;
        regressionDataSet.testIterator = testIterator;
        regressionDataSet.labelsMask = labelsMask;
        regressionDataSet.countInput = trainSets.getFirst().numInputs();
        regressionDataSet.countOutput = trainSets.getFirst().numOutcomes();
        regressionDataSet.sequenceLength = countInput;

        System.out.println("Train set size: " + trainSets.size());
        System.out.println("Test set size: " + testSets.size());
        return regressionDataSet;
    }

    public static double[] getColumn(double[][] input, int columnIndex, int lastIndex) {
        if (lastIndex > input.length) throw new IllegalArgumentException("Last index must be less than array length");
        double[] column = new double[lastIndex];
        for (int i = 0; i < lastIndex; i++) {
            column[i] = input[i][columnIndex];
        }
        return column;
    }

    public static double[][] refactor(double[][] in, Differentiator differentiator, boolean save, StandardizeNormalizer normalizer, int countInput) {
        double[][] diff = differentiator.differentiate(in, NUMBER_OF_DIFFERENTIATIONS, save);

        for (int i = 0; i < diff.length; i++) {
            for (int j = COUNT_VALUES_FOR_DIFFERENTIATION; j < COUNT_VALUES_NOT_VOLATILE_WITHOUT_MA; j++) {
                double value = Math.log(diff[i][j]);
                if (Double.isInfinite(value) || Double.isNaN(value)) {
                    if (i == 0) {
                        diff[i][j] = new Random().nextDouble(0, 2);
                    } else {
                        diff[i][j] = diff[i-1][j] + new Random().nextDouble(-1, 1);
                    }
                } else {
                    diff[i][j] = value;
                }
            }
        }

        double[] orient = getColumn(in, POSITION_OF_PRICES_NORMALIZER_IND, in.length);
        for (int i = COUNT_VALUES_NOT_VOLATILE_WITHOUT_MA; i < COUNT_VALUES_NOT_VOLATILE_WITHOUT_MA + MOVING_AVERAGES_COUNT_FOR_DIFF_WITH_PRICE_VALUES; i++) {
            for (int j = 0; j < diff.length; j++) {
                diff[j][i] = orient[j + 1] - diff[j][i];
            }
        }

        if (countInput==0)
            countInput = diff.length;

        normalizer.fit(diff, countInput);
        normalizer.transform(diff);

        return diff;
    }
    public static double[][] refactor(double[][] in, Differentiator differentiator, boolean save, StandardizeNormalizer normalizer) {
         return refactor(in, differentiator, save, normalizer, 0);
    }

    public static void main(String[] args) {
        CSVCoinDataSet cs = new CSVCoinDataSet(Coin.BTCUSDT, TimeFrame.ONE_HOUR);
        cs.load();
        RegressionDataSet regressionDataSet = RegressionDataSet.prepareTrainSet(Coin.BTCUSDT, DataLength.L100_6, cs);
        DataSet set = regressionDataSet.getTrainIterator().next(1);
        INDArray input = set.getFeatures();
        double[][] matrix = input.slice(0).toDoubleMatrix();
        int index = 0;
        for (double[] d : matrix) {
            XYSeries series = new XYSeries("param n." + index++);
            for (int i = 0; i < d.length; i++) {
                series.add(i, d[i]);
            }
            DataVisualisation.visualize("param n." + index, "count", "value", series);
            System.out.println(Arrays.toString(d));
        }
    }

}
