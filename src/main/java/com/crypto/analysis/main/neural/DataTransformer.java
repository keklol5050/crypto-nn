package com.crypto.analysis.main.neural;

import com.crypto.analysis.main.data_utils.BinanceDataMultipleInstance;
import com.crypto.analysis.main.enumerations.TimeFrame;
import com.crypto.analysis.main.vo.DataObject;

import java.util.Arrays;

public class DataTransformer {
    private final double[][] input;
    private final double[] output;

    public DataTransformer(double[][] input, double[] output) {
        this.input = input;
        this.output = output;
    }

    public DataTransformer(double[][] input) {
        this.input = input;
        this.output = null;
    }

    public static void main(String[] args) {
        DataObject[] pr = BinanceDataMultipleInstance.getLatestInstances("BTCUSDT", TimeFrame.ONE_HOUR);

        double[][] inputData = new double[pr.length - 1][];
        for (int i = 0; i < pr.length - 1; i++) {
            inputData[i] = pr[i].getParamArray();
        }
        double[] outputData = pr[inputData.length].getParamArray();

        DataTransformer normalizer = new DataTransformer(inputData, outputData);
        double[][] normalizedData = normalizer.transformInput();

        for (double[] row : inputData) {
            System.out.println(Arrays.toString(row));
        }
        System.out.println(Arrays.toString(outputData));
        System.out.println();
        for (double[] row : normalizedData) {
            System.out.println(Arrays.toString(row));
        }
        double[] normalizedOutput = normalizer.transformOutput();
        System.out.println(Arrays.toString(normalizedOutput));

    }

    public double[][] transformInput() {
        double[][] output = new double[input.length][];

        double firstOpen = 0;
        double firstHigh = 0;
        double firstLow = 0;
        double firstClose = 0;

        double lastOpen = 0;
        double lastHigh = 0;
        double lastLow = 0;
        double lastClose = 0;

        for (int i = 0; i < input.length; i++) {
            double[] in = input[i];
            double[] out = new double[input[i].length + 4];
            if (i != 0) {
                out[0] = in[0] - firstOpen;
                out[1] = in[1] - firstHigh;
                out[2] = in[2] - firstLow;
                out[3] = in[3] - firstClose;

                out[4] = in[0] - lastOpen;
                out[5] = in[1] - lastHigh;
                out[6] = in[2] - lastLow;
                out[7] = in[3] - lastClose;
            } else {
                firstOpen = in[0];
                firstHigh = in[1];
                firstLow = in[2];
                firstClose = in[3];
            }
            if (input[i].length - 4 >= 0) System.arraycopy(in, 4, out, 8, input[i].length - 4);
            lastOpen = in[0];
            lastHigh = in[1];
            lastLow = in[2];
            lastClose = in[3];
            output[i] = out;
        }
        return output;
    }

    public double[] transformOutput() {
        if (output == null) throw new UnsupportedOperationException();
        double[] out = new double[output.length + 4];
        for (int i = 0; i < 4; i++) {
            out[i] = output[i] - input[0][i];
        }
        for (int i = 0; i < 4; i++) {
            out[i + 4] = output[i] - input[input.length - 1][i];
        }
        if (output.length - 4 >= 0) System.arraycopy(output, 4, out, 8, output.length - 4);
        return out;
    }
}