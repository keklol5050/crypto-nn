package com.crypto.analysis.main.data;

import com.crypto.analysis.main.data_utils.BinanceDataMultipleInstance;
import com.crypto.analysis.main.enumerations.Coin;
import com.crypto.analysis.main.enumerations.TimeFrame;
import com.crypto.analysis.main.vo.DataObject;

import java.util.Arrays;

public class DataRefactor {
    private final double[][] data;
    private final int countParams;

    private double[][] input;
    private double[][] output;
    private final int countInput;
    private final int countOutput;

    public DataRefactor(double[][] data, int countInput, int countOutput) {
        this.data = data;
        this.countInput = countInput;
        this.countOutput = countOutput;
        this.countParams = data[0].length;
        init();
    }

    public void init() {
        if (countInput+countOutput!=data.length) throw new ArithmeticException("Parameters count are not equals");
        if (countOutput<1 || countInput<1) throw new IllegalArgumentException("Parameters cannot be zero or negative");

        double[][] values = new double[data.length][];
        values[0] = new double[countParams];
        for (int i=0; i<countParams; i++) {
            values[0][i] = 1;
        }
        double[] firstValues = new double[countParams];
        System.arraycopy(data[0], 0, firstValues, 0, firstValues.length);

        for (int i = 1; i < data.length; i++) {
            double[] currentParams = data[i];
            double[] result = new double[countParams];
            for (int j = 0; j < countParams; j++) {
                result[j] = calculateChange(firstValues[j], currentParams[j]);
            }
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
        return newValue-oldValue;
    }

    public static void main(String[] args) {
        DataObject[] pr = BinanceDataMultipleInstance.getLatestInstances(Coin.BTCUSDT, TimeFrame.ONE_HOUR, 30);
        double[][] in = new double[pr.length][];
        for (int i = 0; i < pr.length; i++) {
            in[i] = pr[i].getParamArray();
        }
        for (double[] i : in) {
            System.out.println(Arrays.toString(i));
        }
        System.out.println();
        System.out.println("=============================================================================================");
        System.out.println("=============================================================================================");
        System.out.println();
        DataRefactor normalizer = new DataRefactor(in, 25, 5);
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