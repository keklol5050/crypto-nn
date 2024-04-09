package com.crypto.analysis.main.view;

import com.crypto.analysis.main.Differentiator;
import com.crypto.analysis.main.core.data.refactor.Transposer;
import com.crypto.analysis.main.core.data.train.TrainDataSet;
import com.crypto.analysis.main.core.data_utils.normalizers.robust.RobustScaler;
import com.crypto.analysis.main.core.data_utils.select.StaticData;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.binance.BinanceDataMultipleInstance;
import com.crypto.analysis.main.core.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.core.model.ModelLoader;
import com.crypto.analysis.main.core.ndata.CSVCoinDataSet;
import com.crypto.analysis.main.core.vo.DataObject;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.crypto.analysis.main.core.data_utils.select.StaticData.NUMBER_OF_DIFFERENTIATIONS;

public class ModelAccessor {
    private final Coin coin;
    private final MultiLayerNetwork model;
    private final int numInputs = StaticData.MODEL_NUM_INPUTS;
    private final int numOutputs = StaticData.MODEL_NUM_OUTPUTS;
    private final RobustScaler normalizer;
    private final FundamentalDataUtil fdUtil;
    private final Differentiator differentiator;

    public ModelAccessor(Coin coin, MultiLayerNetwork model, RobustScaler normalizer, FundamentalDataUtil fdUtil) {
        this.coin = coin;
        this.model = model;
        this.normalizer = normalizer;
        this.fdUtil = fdUtil;

        this.differentiator = new Differentiator();
    }

    public double[][] predict(DataLength dl, TimeFrame tf, DataObject[] data, boolean revert) {
        fdUtil.init();

        int countInput = dl.getCountInput() - 1;
        int countOutput = dl.getCountOutput();

        if (data == null) {
            data = BinanceDataMultipleInstance.getLatestInstances(coin, tf, countOutput, fdUtil);
        } else {
            if (data.length != countInput) {
                DataObject[] newData = new DataObject[countInput];
                if (data.length - (data.length - countInput) >= 0)
                    System.arraycopy(data, data.length - countInput, newData, data.length - countInput, data.length - (data.length - countInput));
                data = newData;
            }
        }

        double[][] input = new double[data.length][];
        for (int i = 0; i < data.length; i++) {
            input[i] = data[i].getParamArray();
        }

        double[][] diff = differentiator.differentiate(input, NUMBER_OF_DIFFERENTIATIONS, true);

        normalizer.setCountInputs(countInput);
        normalizer.setCountOutputs(countOutput);

        normalizer.fit(diff);
        normalizer.transform(diff);

        input = Transposer.transpose(diff);

        normalizer.changeBinding(diff, input);

        INDArray labelsMask = Nd4j.zeros(1, diff[0].length);
        for (int i = 0; i < dl.getCountOutput(); i++) {
            labelsMask.putScalar(new int[]{0, i}, 1.0);
        }

        INDArray inputNDArray = Nd4j.createFromArray(new double[][][]{input});
        INDArray output = model.output(inputNDArray, false, null, labelsMask);

        double[][] predMatrix = output.slice(0).toDoubleMatrix();
        normalizer.revertLabels(input, predMatrix);

        double[][] predicted = new double[numOutputs][countOutput];
        for (int j = 0; j < numOutputs; j++) {
            System.arraycopy(predMatrix[j], 0, predicted[j], 0, countOutput);
        }

        if (revert) {
            predicted = Transposer.transpose(predicted);

            double[][] finalData = new double[predicted.length + diff.length][];
            System.arraycopy(diff, 0, finalData, 0, diff.length);
            System.arraycopy(predicted, 0, finalData, diff.length, predicted.length);

            finalData = differentiator.restoreData(diff, finalData);

            predicted = new double[dl.getCountOutput()][];
            int index = 0;
            for (int i = finalData.length-dl.getCountOutput(); i < finalData.length; i++) {
                predicted[index++] = finalData[i];
            }

            predicted = Transposer.transpose(predicted);
        }

        return predicted;
    }

    public ArrayList<double[][]> allPredicts(TimeFrame tf, boolean revert) {
        ArrayList<double[][]> predictions = new ArrayList<>();

        double[][] prediction50c = predict(DataLength.S30_3, tf, null, revert);
        double[][] prediction80c = predict(DataLength.L60_6, tf, null, revert);
        double[][] prediction110c = predict(DataLength.X100_10, tf, null, revert);

        predictions.add(prediction50c);
        predictions.add(prediction80c);
        predictions.add(prediction110c);

        return predictions;
    }

    public double[][] getPredictionsMedian(TimeFrame tf, boolean revert, DataObject[] data) {
        if (data.length != DataLength.MAX_INPUT_LENGTH)
            throw new IllegalArgumentException("Data length is not correct");

        double[][] prediction50c = predict(DataLength.S30_3, tf, data, revert);
        double[][] prediction80c = predict(DataLength.L60_6, tf, data, revert);
        double[][] prediction110c = predict(DataLength.X100_10, tf, data, revert);

        double[][] median = new double[prediction110c.length][];

        for (int i = 0; i < median.length; i++) {
            median[i] = new double[DataLength.MAX_OUTPUT_LENGTH];
            for (int j = 0; j < DataLength.S30_3.getCountOutput(); j++) {
                median[0][j] = (prediction50c[0][j] + prediction80c[0][j] + prediction110c[0][j]) / 3.0;
            }

            for (int j = DataLength.S30_3.getCountOutput(); j < DataLength.L60_6.getCountOutput(); j++) {
                median[0][j] = (prediction80c[0][j] + prediction110c[0][j]) / 2.0;
            }

            System.arraycopy(prediction110c[i], DataLength.L60_6.getCountOutput(),
                    median[i], DataLength.L60_6.getCountOutput(), DataLength.MIN_OUTPUT_LENGTH);
        }

        return median;
    }

    public void fit(DataLength dl, CSVCoinDataSet setD, int batchSize, int numEpochs) {
        setD.load();
        TrainDataSet trainSet = TrainDataSet.prepareTrainSet(Coin.BTCUSDT, dl, setD);

        LinkedList<double[][]> inputList = trainSet.getTrainData();
        LinkedList<double[][]> outputList = trainSet.getTrainResult();

        int sequenceLength = inputList.get(0)[0].length;

        INDArray labelsMask = Nd4j.zeros(1, sequenceLength);
        for (int i = 0; i < dl.getCountOutput(); i++) {
            labelsMask.putScalar(new int[]{0, i}, 1.0);
        }

        List<DataSet> sets = new ArrayList<>();
        for (int i = 0; i < inputList.size(); i++) {
            double[][] inputData = inputList.get(i);
            double[][] outputData = outputList.get(i);

            INDArray input = Nd4j.createFromArray(new double[][][]{inputData});
            INDArray labels = Nd4j.createFromArray(new double[][][]{outputData});

            DataSet set = new DataSet(input, labels, null, labelsMask);
            sets.add(set);
        }

        DataSetIterator iterator = new ListDataSetIterator<>(sets, batchSize);

        for (int i = 0; i < numEpochs; i++) {
            if (i % StaticData.COUNT_EPOCHS_TO_SAVE_MODEL == 0 && i > 0) {
                ModelLoader.saveModel(model, StaticData.PATH_TO_MODEL);
                System.out.println("Saved at epoch: " + model.getEpochCount());
            }
            model.fit(iterator);
            System.gc();
        }

        ModelLoader.saveModel(model, StaticData.PATH_TO_MODEL);
    }
}
