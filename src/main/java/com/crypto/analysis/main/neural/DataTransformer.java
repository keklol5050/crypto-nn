package com.crypto.analysis.main.neural;

import com.crypto.analysis.main.data_utils.BinanceDataMultipleInstance;
import com.crypto.analysis.main.enumerations.TimeFrame;
import com.crypto.analysis.main.vo.DataObject;

import java.util.Arrays;

public class DataTransformer {
    private final DataObject[] data;
    private final int countParams;

    private double[][] input;
    private double[] output;

    public DataTransformer(DataObject[] data) {
        this.data = data;
        this.countParams = data[0].getParamArray().length;
        init();
    }


    public void init() {
        double[][] values = new double[data.length][];

        double[] firstValues = new double[4];
        double[] lastValues = new double[countParams];

        double[] first = data[0].getParamArray();
        System.arraycopy(first, 0, firstValues, 0, 4);
        System.arraycopy(first, 0, lastValues, 0, lastValues.length);
        values[0] = new double[countParams+4];

        for (int i = 1; i < data.length; i++) {
            double[] currentParams = data[i].getParamArray();
            double[] result = new double[countParams+4];
            for (int j = 0; j < countParams; j++) {
                result[j] = calculateChange(lastValues[j], currentParams[j]);
            }
            for (int j = countParams; j < result.length; j++) {
                int index = j - countParams;
                result[j] = calculateChange(firstValues[index], currentParams[index]);
            }
            System.arraycopy(currentParams, 0, lastValues, 0, countParams);
            values[i] = result;
        }

        input = new double[values.length-1][];
        System.arraycopy(values, 0, input, 0, input.length);

        output = values[values.length-1];
    }

    public double[][] transformInput() {
        return input;
    }

    public double[] transformOutput() {
        return output;
    }

    private double calculateChange(double oldValue, double newValue){
        if (oldValue==0) return newValue;
        if (newValue==0) return oldValue;
        return ((newValue - oldValue) / Math.abs(oldValue)) * 100;
    }


    public static void main(String[] args) {
        DataObject[] pr = BinanceDataMultipleInstance.getLatestInstances("BTCUSDT", TimeFrame.ONE_HOUR);

        double[][] inputData = new double[pr.length - 1][];
        for (int i = 0; i < pr.length - 1; i++) {
            inputData[i] = pr[i].getParamArray();
        }
        double[] outputData = pr[inputData.length].getParamArray();

        for (double[] row : inputData) {
            System.out.println(Arrays.toString(row));
        }
        System.out.println(Arrays.toString(outputData));
        System.out.println();
        System.out.println("=============================================================================================");
        System.out.println("=============================================================================================");
        System.out.println();
        DataTransformer normalizer = new DataTransformer(pr);
        double[][] normalizedData = normalizer.transformInput();

        for (double[] row : normalizedData) {
            System.out.println(Arrays.toString(row));
        }

        double[] normalizedOutput = normalizer.transformOutput();
        System.out.println(Arrays.toString(normalizedOutput));

        System.out.println(normalizedData.length);
        System.out.println(normalizedOutput.length);

    }

}