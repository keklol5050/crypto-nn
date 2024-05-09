package com.crypto.analysis.main;

import com.crypto.analysis.main.core.data_utils.normalizers.Differentiator;
import com.crypto.analysis.main.core.data_utils.normalizers.StandardizeNormalizer;
import com.crypto.analysis.main.core.data_utils.normalizers.Transposer;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.regression.RegressionDataSet;
import com.crypto.analysis.main.core.data_utils.utils.BinanceDataUtil;
import com.crypto.analysis.main.core.data_utils.utils.mutils.DataVisualisation;
import com.crypto.analysis.main.core.data_utils.utils.mutils.ModelLoader;
import com.crypto.analysis.main.core.vo.DataObject;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Arrays;

import static com.crypto.analysis.main.core.data_utils.select.StaticData.MASK_OUTPUT;

public class RealTimeTest {

    public static void main(String[] args) {
        DataLength dl = DataLength.L100_6;
        int countInput = dl.getCountInput() - 1;
        int countOutput = dl.getCountOutput();

        DataObject[] data = BinanceDataUtil.getLatestInstances(Coin.BTCUSDT, TimeFrame.ONE_HOUR, dl.getCountInput()+1, null);

        double[][] dataArr = new double[data.length-1][];
        for (int i = 0; i < dataArr.length; i++) {
            dataArr[i] = data[i].getParamArray();
        }

        double firstOpen = dataArr[0][0];

        StandardizeNormalizer normalizer = new StandardizeNormalizer(MASK_OUTPUT, countOutput, dataArr[0].length);
        Differentiator differentiator = new Differentiator();

        double[][] inputArr = RegressionDataSet.refactor(dataArr, differentiator, true, normalizer);
        double[][] newInputArr = Transposer.transpose(inputArr);

        INDArray input = Nd4j.createFromArray(new double[][][]{newInputArr});

        INDArray labelsMask = Nd4j.zeros(1, countInput);
        for (int i = 0; i < countOutput; i++) {
            labelsMask.putScalar(new int[]{0, i}, 1.0);
        }

        MultiLayerNetwork network = ModelLoader.loadNetwork("D:\\model13.zip");
        network.init();

        INDArray output = network.output(input, false, null, labelsMask);

        double[][] predMatrix = output.slice(0).toDoubleMatrix();
        double[][] predicted = new double[MASK_OUTPUT.length][countOutput];
        for (int j = 0; j < MASK_OUTPUT.length; j++) {
            System.arraycopy(predMatrix[j], 0, predicted[j], 0, countOutput);
        }

        predicted = Transposer.transpose(predicted);

        normalizer.revertFeatures(inputArr);
        normalizer.revertLabels(inputArr, predicted);

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
            opens[i] = restored[i-1][0];
        }

        for (double[] d : restored) {
            System.out.println(Arrays.toString(d));
        }
        restored = Transposer.transpose(restored);

        DataVisualisation.visualizeChart(opens, restored[1], restored[2], restored[0], data[0].getCandle().getOpenTime(), TimeFrame.ONE_HOUR, countOutput);
        System.out.println(network.summary());
    }

}
