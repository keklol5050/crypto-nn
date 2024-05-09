package com.crypto.analysis.main.core.data_utils.normalizers;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.BinanceDataUtil;
import com.crypto.analysis.main.core.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.core.vo.DataObject;

import java.util.Arrays;
import java.util.HashMap;

import static com.crypto.analysis.main.core.data_utils.select.StaticData.COUNT_VALUES_FOR_DIFFERENTIATION;
import static com.crypto.analysis.main.core.data_utils.select.StaticData.MASK_OUTPUT;

public class Differentiator {
    private final HashMap<double[][], double[][]> firstValuesCash;

    public Differentiator() {
        this.firstValuesCash = new HashMap<>();
    }

    private double[][] differentiate(double[][] data) {
        double[][] diff = new double[data.length - 1][data[0].length];
        for (int i = 0; i < data.length - 1; i++) {
            for (int j = 0; j < COUNT_VALUES_FOR_DIFFERENTIATION; j++) {
                diff[i][j] = data[i + 1][j] - data[i][j];
            }
            System.arraycopy(data[i + 1], COUNT_VALUES_FOR_DIFFERENTIATION, diff[i], COUNT_VALUES_FOR_DIFFERENTIATION, data[i + 1].length - COUNT_VALUES_FOR_DIFFERENTIATION);
        }
        return diff;
    }

    private double[][] integrate(double[][] differencedData, double[] firstValue) {
        double[][] integratedData = new double[differencedData.length + 1][differencedData[0].length];
        for (int j = 0; j < differencedData[0].length; j++) {
            integratedData[0][j] = firstValue[j];
        }
        for (int i = 1; i < integratedData.length; i++) {
            for (int j = 0; j < integratedData[0].length; j++) {
                integratedData[i][j] = integratedData[i - 1][j] + differencedData[i - 1][j];
            }
        }
        return integratedData;
    }

    public double[][] differentiate(double[][] data, int numberOfDifferentiations, boolean save) {

        if (numberOfDifferentiations < 0)
            throw new IllegalArgumentException("Count of differentiations must be greater than 0!");
        if (data == null || data.length == 0)
            throw new IllegalArgumentException("Data must be not empty and not null!");
        if (numberOfDifferentiations == 0) return data;

        double[][] firstValues = new double[numberOfDifferentiations][MASK_OUTPUT.length];

        for (int i = 0; i < MASK_OUTPUT.length; i++) {
            firstValues[0][i] = data[0][MASK_OUTPUT[i]];
        }
        double[][] lastDiff = differentiate(data);

        for (int i = 1; i < numberOfDifferentiations; i++) {
            for (int j = 0; j < MASK_OUTPUT.length; j++) {
                firstValues[i][j] = lastDiff[0][MASK_OUTPUT[j]];
            }
            lastDiff = differentiate(lastDiff);
        }

        if (save) firstValuesCash.put(data, firstValues);
        return lastDiff;
    }

    public double[][] restoreData(double[][] key, double[][] data) {
        if (key == null || key.length == 0) throw new IllegalArgumentException("Key must be not empty and not null!");
        if (data == null || data.length == 0)
            throw new IllegalArgumentException("Predicts must be not empty and not null!");

        double[][] firstValues = firstValuesCash.get(key);
        if (firstValues.length == 0) return data;

        double[][] lastRestored = integrate(data, firstValues[firstValues.length - 1]);

        for (int i = firstValues.length - 2; i >= 0; i--) {
            lastRestored = integrate(lastRestored, firstValues[i]);
        }

        return lastRestored;
    }

    public static void main(String[] args) {
        Differentiator differentiator = new Differentiator();
        DataObject[] objs = BinanceDataUtil.getLatestInstances(Coin.BTCUSDT, TimeFrame.ONE_HOUR, 40, new FundamentalDataUtil());
        double[][] m = new double[objs.length][];
        for (int i = 0; i < objs.length; i++) {
            double[] val = objs[i].getParamArray();
            System.out.println(Arrays.toString(val));
            m[i] = val;
        }
        System.out.println();
        double[][] diff = differentiator.differentiate(m, 10, true);
        double[][] labels = new double[diff.length][MASK_OUTPUT.length];
        for (int i = 0; i < diff.length; i++) {
            for (int j = 0; j < labels[0].length; j++) {
                labels[i][j] = diff[i][MASK_OUTPUT[j]];
            }
        }
        labels = differentiator.restoreData(m, labels);
        for (double[] d : diff) {
            System.out.println(Arrays.toString(d));
        }
        System.out.println();
        System.out.println();
        for (double[] d : labels) {
            System.out.println(Arrays.toString(d));
        }
    }
}