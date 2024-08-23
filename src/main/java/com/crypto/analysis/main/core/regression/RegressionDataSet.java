package com.crypto.analysis.main.core.regression;

import ai.djl.Device;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDArrays;
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

import static com.crypto.analysis.main.core.vo.DataObject.*;
import static com.crypto.analysis.main.core.vo.ModelParams.*;

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

    private static final Logger logger = LoggerFactory.getLogger(RegressionDataSet.class);
    private static DescriptiveStatistics stats;

    private RegressionDataSet(Coin coin, DataLength dl) {
        this.coin = coin;
        this.dl = dl;
    }

    public static RegressionDataSet prepareTrainSet(Coin coin, DataLength dl, CSVCoinDataSet set, Device device, int batchSize, boolean split) {
        logger.info("Preparing train set..");
        RegressionDataSet regressionDataSet = new RegressionDataSet(coin, dl);

        TrainDataCSV trainData = new TrainDataCSV(coin, set.getTf(), dl, set);

        return getTrainDataSet(dl, regressionDataSet, trainData.getData(), device, batchSize, split);
    }

    public static RegressionDataSet prepareTrainSet(Coin coin, DataLength dl, TimeFrame tf, Device device, int batchSize, boolean split) {
        logger.info("Preparing train set (Binance)..");
        RegressionDataSet regressionDataSet = new RegressionDataSet(coin, dl);

        TrainDataBinance trainDataBinance = new TrainDataBinance(coin, tf, dl);

        return getTrainDataSet(dl, regressionDataSet, trainDataBinance.getData(), device, batchSize, split);
    }

    @NotNull
    public static RegressionDataSet getTrainDataSet(DataLength dl,
                                                    RegressionDataSet regressionDataSet,
                                                    ArrayList<DataObject[]> data,
                                                    Device device,
                                                    int batchSize,
                                                    boolean split) {
        int numInputSteps = dl.getCountInput();
        int numOutputSteps = dl.getCountOutput();
        int numFeatures = data.getFirst()[0].getParamArray().length;

        ArrayList<float[][]> dataArr = new ArrayList<>();
        int start = 0;
        if (data.getFirst()[0].getInterval() == TimeFrame.FIFTEEN_MINUTES){
            start = switch (dl) {
                case S100_5 -> 0;
                case L120_6 -> 0;
                case X180_9 -> 10000;
                case XL240_12 -> 15000;
                default -> throw new IllegalStateException("Unexpected value: " + dl);
            };
        }
        for (int i = start; i < data.size(); i++) {
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
            if (numInputSteps + numOutputSteps != in.length)
                throw new ArithmeticException("Parameters count are not equals");
            if (numOutputSteps < 1 || numInputSteps < 1)
                throw new IllegalArgumentException("Parameters cannot be zero or negative");

            float[][] ref = refactor(in, normalizer, numInputSteps, true);

            float[][] input = new float[numInputSteps][];
            System.arraycopy(ref, 0, input, 0, input.length);

            float[][] output = new float[numOutputSteps][];
            System.arraycopy(ref, input.length, output, 0, output.length);

            float[][] finalOutput = new float[numOutputSteps][];
            for (int i = 0; i < finalOutput.length; i++) {
                finalOutput[i] = new float[MASK_OUTPUT.length];
                for (int j = 0; j < finalOutput[i].length; j++) {
                    finalOutput[i][j] = output[i][MASK_OUTPUT[j]];
                }
            }
            input = Transposer.transpose(input);
            finalOutput = Transposer.transpose(finalOutput);

            allInputs.add(input);
            allOutputs.add(finalOutput[0]); //TODO
        }

        int numSamples = allInputs.size();
        int firstPart = numSamples/2;
        int secondPart = numSamples - firstPart;
        int max = split ? numSamples - COUNT_TEST_VALUES : numSamples;

        float[] dataFlatFirst = new float[firstPart * numFeatures * numInputSteps];
        for (int i = 0; i < firstPart; i++) {
            for (int j = 0; j < numFeatures; j++) {
                for (int k = 0; k < numInputSteps; k++) {
                    dataFlatFirst[i * numFeatures * numInputSteps + j * numInputSteps + k] = allInputs.get(i)[j][k];
                }
            }
        }

        float[] flatLabelsFirst = new float[firstPart * numOutputSteps];
        for (int i = 0; i < firstPart; i++) {
            System.arraycopy(allOutputs.get(i), 0, flatLabelsFirst, i * numOutputSteps, numOutputSteps);
        }

        NDArray inFirst = manager.create(dataFlatFirst).reshape(new Shape(firstPart, numFeatures, numInputSteps));
        NDArray outFirst = manager.create(flatLabelsFirst).reshape(new Shape(firstPart, numOutputSteps));

        System.gc();

        float[] dataFlatSecond = new float[secondPart * numFeatures * numInputSteps];
        for (int i = 0; i < secondPart; i++) {
            for (int j = 0; j < numFeatures; j++) {
                for (int k = 0; k < numInputSteps; k++) {
                    dataFlatSecond[i * numFeatures * numInputSteps + j * numInputSteps + k] = allInputs.get(i+firstPart)[j][k];
                }
            }
        }

        float[] flatLabelsSecond = new float[secondPart * numOutputSteps];
        for (int i = 0; i < secondPart; i++) {
            System.arraycopy(allOutputs.get(i+firstPart), 0, flatLabelsSecond, i * numOutputSteps, numOutputSteps);
        }

        NDArray inSecond = manager.create(dataFlatSecond).reshape(new Shape(secondPart, numFeatures, numInputSteps));
        NDArray outSecond = manager.create(flatLabelsSecond).reshape(new Shape(secondPart, numOutputSteps));

        System.gc();

        NDArray in = NDArrays.concat(new NDList(inFirst, inSecond));
        NDArray out = NDArrays.concat(new NDList(outFirst, outSecond));

        NDList input = split ? in.split(new long[]{max}) : new NDList(in);
        NDList output = split ? out.split(new long[]{max}) : new NDList(out);

        ArrayDataset trainSet = new ArrayDataset.Builder()
                .setData(input.getFirst())
                .optLabels(output.getFirst())
                .setSampling(batchSize, true)
                .optDevice(device)
                .build();

        ArrayDataset testSet = split ? new ArrayDataset.Builder()
                .setData(input.getLast())
                .optLabels(output.getLast())
                .setSampling(batchSize, false)
                .optDevice(device)
                .build() : null;

        regressionDataSet.trainSet = trainSet;
        regressionDataSet.testSet = testSet;
        regressionDataSet.numFeatures = numFeatures;
        regressionDataSet.inputSteps = numInputSteps;
        regressionDataSet.outputSteps = numOutputSteps;

        logger.info("Train set size: {}", trainSet.size());
        logger.info("Test set size: {}", testSet != null ? testSet.size() : 0);
        for (String s : Arrays.asList(String.format("Regression set created with params: numFeatures - %d, numInputSteps - %d, numOutputSteps - %d, batchSize - %d",
                numFeatures, numInputSteps, numOutputSteps, batchSize), "Regression set data length: " + dl)) {
            logger.info(s);
        }

        System.gc();
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

    public static float[][] refactor(float[][] in, MaxAbsScaler normalizer, int countInput, boolean clear) {
        float[][] ref = new float[in.length][in[0].length];
        for (int i = 0; i < in.length; i++) {
            System.arraycopy(in[i], 0, ref[i], 0, in[i].length);
        }


        float[] orient = getColumn(ref, POSITION_OF_PRICES_NORMALIZER_IND, ref.length);
        for (int i = COUNT_VALUES_NOT_VOLATILE_WITHOUT_MA; i < COUNT_VALUES_NOT_VOLATILE_WITHOUT_MA + MOVING_AVERAGES_COUNT; i++) {
            for (int j = 0; j < ref.length; j++) {
                ref[j][i] = orient[j] - ref[j][i];
            }
        }

        for (int i = 0; i < COUNT_PRICES_VALUES; i++) {
            stats = new DescriptiveStatistics();
            for (float[] doubles : ref) {
                stats.addValue(doubles[i]);
            }

            float mean = (float) stats.getMean();
            for (int j = 0; j < ref.length; j++) {
                ref[j][i] = ref[j][i] - mean;
            }
        }

        for (int i = BTCDOM_POSITION; i < DEFAULT_NUM_FEATURES; i++) {
            stats = new DescriptiveStatistics();
            for (int j = 0; j < countInput; j++) {
                stats.addValue(ref[j][i]);
            }

            float mean = (float) stats.getMean();
            for (int j = 0; j < ref.length; j++) {
                ref[j][i] = ref[j][i] - mean;
            }
        }

        if (countInput==0)
            countInput = ref.length;

        normalizer.fit(ref, countInput);
        normalizer.transform(ref, clear);

        return ref;
    }

    public static float[][] refactor(float[][] in, MaxAbsScaler normalizer) {
        return refactor(in, normalizer, 0, false);
    }
}
