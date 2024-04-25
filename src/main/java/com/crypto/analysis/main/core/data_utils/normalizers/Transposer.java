package com.crypto.analysis.main.core.data_utils.normalizers;

public class Transposer {
    public static double[][] transpose(double[][] input) {
        double[][] output = new double[input[0].length][input.length];
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                output[j][i] = input[i][j];
            }
        }
        return output;
    }

    public static double[][] transpose(double[][] input, int length) {
        double[][] output = new double[input[0].length][length];
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                output[j][i] = input[i][j];
            }
        }
        return output;
    }

    public static double[][] transposeSingle(double[] input, int length) {
        double[][] output = new double[input.length][length];
        for (int i = 0; i < input.length; i++) {
            output[i][0] = input[i];
        }
        return output;
    }
}
