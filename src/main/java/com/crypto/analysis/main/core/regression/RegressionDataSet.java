package com.crypto.analysis.main.core.regression;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.Shape;
import ai.djl.training.dataset.ArrayDataset;
import ai.djl.training.dataset.Record;
import com.crypto.analysis.main.core.data_utils.normalizers.MaxAbsScaler;
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
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jetbrains.annotations.NotNull;
import org.jfree.data.xy.XYSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static com.crypto.analysis.main.core.data_utils.select.StaticData.*;

@Getter
@Setter
public class RegressionDataSet {
    private final Coin coin;
    private final DataLength dl;
    private ArrayDataset trainSet;
    private ArrayDataset testSet;
    private int numFeatures;
    private int outputSteps;
    private int inputSteps;
    private int batchSize;

    private static final Logger logger = LoggerFactory.getLogger(RegressionDataSet.class);

    private RegressionDataSet(Coin coin, DataLength dl) {
        this.coin = coin;
        this.dl = dl;
    }

    public static RegressionDataSet prepareTrainSet(Coin coin, DataLength dl, CSVCoinDataSet set) {
        logger.info("Preparing train set..");
        RegressionDataSet regressionDataSet = new RegressionDataSet(coin, dl);

        TrainDataCSV trainData = new TrainDataCSV(coin, set.getInterval(), dl, set);

        ArrayList<DataObject[]> data = new ArrayList<>(trainData.getData());

        return getTrainDataSet(dl, regressionDataSet, data);
    }

    public static RegressionDataSet prepareTrainSet(Coin coin, DataLength dl, TimeFrame interval, FundamentalDataUtil fdUtil) {
        logger.info("Preparing train set..");
        RegressionDataSet regressionDataSet = new RegressionDataSet(coin, dl);

        TrainDataBinance trainDataBinance = new TrainDataBinance(coin, interval, dl, fdUtil);

        ArrayList<DataObject[]> data = new ArrayList<>(trainDataBinance.getData());

        return getTrainDataSet(dl, regressionDataSet, data);
    }

    @NotNull
    public static RegressionDataSet getTrainDataSet(DataLength dl, RegressionDataSet regressionDataSet, ArrayList<DataObject[]> data) {
        int numInputSteps = dl.getCountInput() - NUMBER_OF_DIFFERENTIATIONS;
        int numOutputSteps = dl.getCountOutput();
        int numFeatures = data.getFirst()[0].getParamArray().length;
        int batchSize = BATCH_SIZE;

        ArrayList<float[][]> dataArr = new ArrayList<float[][]>();
        for (int i = 0; i < data.size(); i++) {
            DataObject[] datum = data.get(i);
            float[][] doArray = new float[datum.length][];
            for (int j = 0; j < datum.length; j++) {
                doArray[j] = datum[j].getParamArray();
            }
            dataArr.add(doArray);
        }

        ArrayList<float[][]> allInputs = new ArrayList<>();
        ArrayList<float[]> allOutputs = new ArrayList<>(); //TODO

        MaxAbsScaler normalizer = new MaxAbsScaler(MASK_OUTPUT, numOutputSteps, numFeatures);

        for (float[][] in : dataArr) {
            if (numInputSteps + numOutputSteps != (in.length - NUMBER_OF_DIFFERENTIATIONS))
                throw new ArithmeticException("Parameters count are not equals");
            if (numOutputSteps < 1 || numInputSteps < 1)
                throw new IllegalArgumentException("Parameters cannot be zero or negative");

            refactor(in, false, normalizer, numInputSteps);

            float[][] input = new float[numInputSteps][];
            System.arraycopy(in, 0, input, 0, input.length);

            float[][] output = new float[numOutputSteps][];
            System.arraycopy(in, input.length, output, 0, output.length);

            float[][] finalOutput = new float[numOutputSteps][];
            for (int i = 0; i < finalOutput.length; i++) {
                finalOutput[i] = new float[MASK_OUTPUT.length];
                for (int j = 0; j < finalOutput[i].length; j++) {
                    finalOutput[i][j] = output[i][MASK_OUTPUT[j]];
                }
            }
            input = Transposer.transpose(input);
            finalOutput = Transposer.transpose(finalOutput);

            normalizer.changeBinding(in, input);
            allInputs.add(input);
            allOutputs.add(finalOutput[0]); //TODO
        }

        int numSamples = allInputs.size();
        int max = numSamples - 1000;

        float[] dataFlat = new float[numSamples * numFeatures * numInputSteps];
        for (int i = 0; i < numSamples; i++) {
            for (int j = 0; j < numFeatures; j++) {
                for (int k = 0; k < numInputSteps; k++) {
                    dataFlat[i * numFeatures * numInputSteps + j * numInputSteps + k] = allInputs.get(i)[j][k];
                }
            }
        }

        float[] flatLabels = new float[numSamples * numOutputSteps];
        for (int i = 0; i < numSamples; i++) {
            System.arraycopy(allOutputs.get(i), 0, flatLabels, i * numOutputSteps, numOutputSteps);
        }

        NDArray in = manager.create(dataFlat).reshape(new Shape(numSamples, numFeatures, numInputSteps));
        NDArray out = manager.create(flatLabels).reshape(new Shape(numSamples, numOutputSteps));

        NDList input = in.split(new long[]{max});
        NDList output = out.split(new long[]{max});

        ArrayDataset trainSet = new ArrayDataset.Builder()
                .setData(input.getFirst())
                .optLabels(output.getFirst())
                .setSampling(batchSize, true)
                .optDevice(DEVICE)
                .build();

        ArrayDataset testSet = new ArrayDataset.Builder()
                .setData(input.getLast())
                .optLabels(output.getLast())
                .setSampling(batchSize, false)
                .optDevice(DEVICE)
                .build();

        regressionDataSet.trainSet = trainSet;
        regressionDataSet.testSet = testSet;
        regressionDataSet.numFeatures = numFeatures;
        regressionDataSet.outputSteps = numOutputSteps;
        regressionDataSet.inputSteps = numInputSteps;
        regressionDataSet.batchSize = batchSize;

        logger.info("Train set size: " + trainSet.size());
        logger.info("Test set size: " + testSet.size());
        return regressionDataSet;
    }

    public static float[] getColumn(float[][] input, int columnIndex, int lastIndex) {
        if (lastIndex > input.length) throw new IllegalArgumentException("Last index must be less than array length");
        float[] column = new float[lastIndex];
        for (int i = 0; i < lastIndex; i++) {
            column[i] = input[i][columnIndex];
        }
        return column;
    }

    public static void refactor(float[][] in, boolean save, MaxAbsScaler normalizer, int countInput) {
        for (int i = 0; i < in.length; i++) {
            for (int j = COUNT_PRICES_VALUES; j < COUNT_VALUES_NOT_VOLATILE_WITHOUT_MA; j++) {
                double value = Math.log(in[i][j]);
                if (Double.isInfinite(value) || Double.isNaN(value)) {
                    if (i == 0) {
                        in[i][j] = new Random().nextFloat(0, 2);
                    } else {
                        in[i][j] = in[i-1][j] + new Random().nextFloat(-1, 1);
                    }
                } else {
                    in[i][j] = (float) value;
                }
            }
        }

        float[] orient = getColumn(in, POSITION_OF_PRICES_NORMALIZER_IND, in.length);
        for (int i = COUNT_VALUES_NOT_VOLATILE_WITHOUT_MA; i < COUNT_VALUES_NOT_VOLATILE_WITHOUT_MA + MOVING_AVERAGES_COUNT_FOR_DIFF_WITH_PRICE_VALUES; i++) {
            for (int j = 0; j < in.length; j++) {
                in[j][i] = orient[j] - in[j][i];
            }
        }

        for (int i = 58; i < in[0].length; i++) {
            DescriptiveStatistics stats = new DescriptiveStatistics();
            for (float[] doubles : in) {
                stats.addValue(doubles[i]);
            }

            float mean = (float) stats.getMean();
            for (int j = 0; j < in.length; j++) {
                in[j][i] = in[j][i] - mean;
            }
        }
        for (int i = 0; i < COUNT_VALUES_FOR_DIFFERENTIATION; i++) {
            DescriptiveStatistics stats = new DescriptiveStatistics();
            for (float[] doubles : in) {
                stats.addValue(doubles[i]);
            }

            float mean = (float) stats.getMean();
            for (int j = 0; j < in.length; j++) {
                in[j][i] = in[j][i] - mean;
            }
        }

        if (countInput==0)
            countInput = in.length;

        normalizer.fit(in, countInput);
        normalizer.transform(in);
    }
    public static void refactor(float[][] in, boolean save, MaxAbsScaler normalizer) {
        refactor(in, save, normalizer, 0);
    }

    public static void main(String[] args) throws IOException {
        CSVCoinDataSet cs = new CSVCoinDataSet(Coin.BTCUSDT, TimeFrame.ONE_HOUR);
        cs.load();
        RegressionDataSet regressionDataSet = RegressionDataSet.prepareTrainSet(Coin.BTCUSDT, DataLength.L120_6, cs);
        Record set = regressionDataSet.getTestSet().get(manager, 1);
        NDArray input = set.getData().singletonOrThrow();
        long[] shape = input.getShape().getShape();
        int index = 0;
        for (int i = 0; i < shape[0]; i++) {
            float[] d = input.get(i).toFloatArray();
            System.out.println(d.length);
            XYSeries series = new XYSeries("param n." + index++);
            for (int j = 0; j < d.length; j++) {
                series.add(j, d[j]);
            }
            DataVisualisation.visualize("param n." + index, "count", "value", series);
            System.out.println(Arrays.toString(d));
        }

        NDArray label = set.getLabels().singletonOrThrow();
        float[] d = label.toFloatArray();
        System.out.println(Arrays.toString(d));
    }

}
