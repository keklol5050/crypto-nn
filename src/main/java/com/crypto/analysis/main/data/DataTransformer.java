package com.crypto.analysis.main.data;

import com.crypto.analysis.main.data_utils.BinanceDataMultipleInstance;
import com.crypto.analysis.main.enumerations.TimeFrame;
import com.crypto.analysis.main.vo.DataObject;

import java.util.Arrays;

public class DataTransformer {
    private final DataObject[] data;
    private final int countParams;

    private double[][] input;
    private double[][] output;
    private final int countInput;
    private final int countOutput;

    public DataTransformer(DataObject[] data, int countInput, int countOutput) {
        this.data = data;
        this.countInput = countInput;
        this.countOutput = countOutput;
        this.countParams = data[0].getPreparedParamArray().length;
        init();
    }


    public void init() {
        if (countInput+countOutput!=data.length) throw new ArithmeticException();
        if (countOutput==0 || countInput==0) throw new IllegalArgumentException();

        double[][] values = new double[data.length][];
        values[0] = new double[countParams];

        double[] lastValues = new double[countParams];
        System.arraycopy(data[0].getPreparedParamArray(), 0, lastValues, 0, lastValues.length);

        for (int i = 1; i < data.length; i++) {
            double[] currentParams = data[i].getPreparedParamArray();
            double[] result = new double[countParams];
            for (int j = 0; j < countParams; j++) {
                result[j] = calculateChange(lastValues[j], currentParams[j]);
            }
            System.arraycopy(currentParams, 0, lastValues, 0, lastValues.length);
            values[i] = result;
        }

        input = new double[countInput][];
        System.arraycopy(values, 0, input, 0, input.length);

        output = new double[countOutput][];
        System.arraycopy(values,countInput, output, 0, output.length);
    }

    public double[][] transformInput() {
        return input;
    }

    public double[][] transformOutput() {
        return output;
    }

    private double calculateChange(double oldValue, double newValue){
        if (oldValue==0) oldValue=1;
        if (newValue==0) newValue=1;
        return ((newValue - oldValue) / Math.abs(oldValue))*100;
    }

    private double calculateChangeInds(double oldValue, double newValue){
        return (newValue - oldValue)/100;
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
        DataTransformer normalizer = new DataTransformer(pr, 25, 5);
        double[][] normalizedData = normalizer.transformInput();

        for (double[] row : normalizedData) {
            System.out.println(Arrays.toString(row));
        }
        System.out.println();
        System.out.println();
        double[][] normalizedOutput = normalizer.transformOutput();

        for (double[] row : normalizedOutput) {
            System.out.println(Arrays.toString(row));
        }

        System.out.println(normalizedData.length);
        System.out.println(normalizedOutput.length);

    }

}