package com.crypto.analysis.main;

import com.crypto.analysis.main.core.data_utils.normalizers.Differentiator;
import com.crypto.analysis.main.core.data_utils.normalizers.StandardizeNormalizer;
import com.crypto.analysis.main.core.data_utils.normalizers.Transposer;
import com.crypto.analysis.main.core.data_utils.select.StaticData;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.BinanceDataUtil;
import com.crypto.analysis.main.core.data_utils.utils.mutils.DataVisualisation;
import com.crypto.analysis.main.core.data_utils.utils.mutils.ModelLoader;
import com.crypto.analysis.main.core.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.core.ndata.CSVCoinDataSet;
import com.crypto.analysis.main.core.regression.RegressionDataSet;
import com.crypto.analysis.main.core.vo.DataObject;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import java.util.ArrayList;
import java.util.Arrays;

import static com.crypto.analysis.main.core.data_utils.select.StaticData.*;

public class ModelAccessor {
    private final Coin coin;
    private final MultiLayerNetwork model;
    private final DataLength dl;
    private final StandardizeNormalizer normalizer;
    private final FundamentalDataUtil fdUtil;
    private final Differentiator differentiator;

    public ModelAccessor(Coin coin, MultiLayerNetwork model, DataLength dl, FundamentalDataUtil fdUtil) {
        this.coin = coin;
        this.model = model;
        this.dl = dl;
        this.fdUtil = fdUtil;

        this.normalizer = new StandardizeNormalizer(MASK_OUTPUT,  dl.getCountOutput(), MODEL_NUM_INPUTS);
        this.differentiator = new Differentiator();
    }

    public double[][] predict(TimeFrame tf, DataObject[] data, boolean revert) {
        if (fdUtil != null)
            fdUtil.init();

        int countInput = dl.getCountInput();
        int countOutput = dl.getCountOutput();


       if (data == null) {
            data = BinanceDataUtil.getLatestInstances(coin, tf, countOutput, fdUtil);
        } else {
            if (data.length != countInput) {
                DataObject[] newData = new DataObject[countInput];
                if (data.length - (data.length - countInput) >= 0)
                    System.arraycopy(data, data.length - countInput, newData, data.length - countInput, data.length - (data.length - countInput));
                data = newData;
            }
        }

        double[][] dataArr = new double[data.length][];
        for (int i = 0; i < data.length; i++) {
            dataArr[i] = data[i].getParamArray();
        }

        double firstOpen = dataArr[0][0];

        double[][] inputArr = RegressionDataSet.refactor(dataArr, differentiator, true, normalizer);
        double[][] newInputArr = Transposer.transpose(inputArr);

        INDArray labelsMask = Nd4j.zeros(1, newInputArr[0].length);
        for (int i = 0; i < dl.getCountOutput(); i++) {
            labelsMask.putScalar(new int[]{0, i}, 1.0);
        }

        INDArray input = Nd4j.createFromArray(new double[][][]{newInputArr});
        INDArray output = model.output(input, true, null, labelsMask);

        double[][] predMatrix = output.slice(0).toDoubleMatrix();
        double[][] predicted = new double[MASK_OUTPUT.length][countOutput];
        for (int j = 0; j < MASK_OUTPUT.length; j++) {
            System.arraycopy(predMatrix[j], 0, predicted[j], 0, countOutput);
        }

        predicted = Transposer.transpose(predicted);

        normalizer.revertFeatures(inputArr);
        normalizer.revertLabels(inputArr, predicted);

        if (revert) {
            double[][] in = new double[inputArr.length + predicted.length][MASK_OUTPUT.length];
            for (int i = 0; i < inputArr.length; i++) {
                for (int j = 0; j < MASK_OUTPUT.length; j++) {
                    in[i][j] = inputArr[i][MASK_OUTPUT[j]];
                }
            }
            for (int i = inputArr.length; i < in.length; i++) {
                for (int j = 0; j < MASK_OUTPUT.length; j++) {
                    in[i][j] = predicted[i - inputArr.length][j];
                }
            }

            double[][] restored = differentiator.restoreData(dataArr, in);

            double[] opens = new double[restored.length];
            opens[0] = firstOpen;
            for (int i = 1; i < opens.length; i++) {
                opens[i] = restored[i-1][2]; // for mask
            }

            restored = Transposer.transpose(restored);

            predicted = new double[][]{opens, restored[0], restored[1], restored[2]}; // for mask
        }

        return predicted;
    }

    public void fit(CSVCoinDataSet setD, int numEpochs) {
        setD.load();
        RegressionDataSet trainSet = RegressionDataSet.prepareTrainSet(Coin.BTCUSDT, dl, setD);

        ListDataSetIterator<DataSet> iterator = trainSet.getTrainIterator();
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

    public static void main(String[] args) {
        Coin coin = Coin.BTCUSDT;
        MultiLayerNetwork model = ModelLoader.loadNetwork("D:/model14.zip");
        DataLength dl = DataLength.L100_6;

        ModelAccessor accessor = new ModelAccessor(coin, model, dl, null);

        DataObject[] data = BinanceDataUtil.getLatestInstances(Coin.BTCUSDT, TimeFrame.ONE_HOUR, dl.getCountInput(), null);

        double[][] pred = accessor.predict(TimeFrame.ONE_HOUR, data, true);
        DataVisualisation.visualizeChart(pred[0], pred[1], pred[2], pred[3], data[0].getCandle().getOpenTime(), TimeFrame.ONE_HOUR, dl.getCountOutput());
    }
}