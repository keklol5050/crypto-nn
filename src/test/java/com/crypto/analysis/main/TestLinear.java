package com.crypto.analysis.main;

import com.crypto.analysis.main.core.data_utils.normalizers.Differentiator;
import com.crypto.analysis.main.core.data_utils.normalizers.MaxAbsScaler;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.BinanceDataUtil;
import com.crypto.analysis.main.core.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.core.regression.RegressionDataSet;
import com.crypto.analysis.main.core.vo.DataObject;

import java.util.Random;

import static com.crypto.analysis.main.core.data_utils.select.StaticData.*;
import static com.crypto.analysis.main.core.data_utils.select.StaticData.NUMBER_OF_DIFFERENTIATIONS;
import static com.crypto.analysis.main.core.regression.RegressionDataSet.getColumn;

public class TestLinear {
/*    public static void main(String[] args) {
        DataObject[] data = BinanceDataUtil.getLatestInstances(Coin.BTCUSDT, TimeFrame.ONE_HOUR, 67, new FundamentalDataUtil());

        double[][] dataArr = new double[data.length][];
        for (int i = 0; i < data.length; i++) {
            dataArr[i] = data[i].getParamArray();
        }

        Differentiator differentiator = new Differentiator();
        MaxAbsScaler scaler = new MaxAbsScaler(MASK_OUTPUT, 6, MODEL_NUM_INPUTS );

        double[][] diff = RegressionDataSet.refactor(dataArr, differentiator, true, scaler);

        double[][] input = new double[60][];
        System.arraycopy(diff, 0, input, 0, input.length);

        double[][] output = new double[6][];
        System.arraycopy(diff, input.length, output, 0, output.length);

        double[][] finalOutput = new double[6][];
        for (int i = 0; i < finalOutput.length; i++) {
            finalOutput[i] = new double[MASK_OUTPUT.length];
            for (int j = 0; j < finalOutput[i].length; j++) {
                finalOutput[i][j] = output[i][MASK_OUTPUT[j]];
            }
        }

        scaler.changeBinding(diff, input);

        scaler.revertFeatures(input);
        scaler.revertLabels(input, finalOutput);

        double[][] diffIn = new double[input.length][];
        for (int i = 0; i < input.length; i++) {
            diffIn[i] = new double[3];
            for (int j = 0; j < diffIn[i].length; j++) {
                diffIn[i][j] = input[i][j+1];
            }
        }
        double[][] restored = differentiator.restoreData(dataArr, diffIn);
    }

    public static double[] scaleByMaxAbsValue(double[] data) {
        double maxAbsValue = maxAbsValue(data);

        double[] scaledData = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            scaledData[i] = data[i] / maxAbsValue;
        }

        return scaledData;
    }

    public static double maxAbsValue(double[] data) {
        double maxAbsValue = 0;
        for (int i = 0; i < data.length-5; i++) {
            double num = data[i];
            maxAbsValue = Math.max(maxAbsValue, Math.abs(num));
        }
        return maxAbsValue;
    }


    public static double[][] refactor(double[][] in, Differentiator differentiator) {
        for (int i = 0; i < in.length; i++) {
            for (int j = COUNT_PRICES_VALUES; j < COUNT_VALUES_NOT_VOLATILE_WITHOUT_MA; j++) {
                double value = Math.log(in[i][j]);
                if (Double.isInfinite(value) || Double.isNaN(value)) {
                    if (i == 0) {
                        in[i][j] = new Random().nextDouble(0, 2);
                    } else {
                        in[i][j] = in[i-1][j] + new Random().nextDouble(-1, 1);
                    }
                } else {
                    in[i][j] = value;
                }
            }
        }

        double[] orient = getColumn(in, POSITION_OF_PRICES_NORMALIZER_IND, in.length);
        for (int i = COUNT_VALUES_NOT_VOLATILE_WITHOUT_MA; i < COUNT_VALUES_NOT_VOLATILE_WITHOUT_MA + MOVING_AVERAGES_COUNT_FOR_DIFF_WITH_PRICE_VALUES; i++) {
            for (int j = 0; j < in.length; j++) {
                in[j][i] = orient[j] - in[j][i];
            }
        }

        double[][] diff = differentiator.differentiate(in, NUMBER_OF_DIFFERENTIATIONS, false);

        return diff;
    }

 */
}