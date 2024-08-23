package com.crypto.analysis.main.core.data_utils.normalizers;

public class Transposer {
    public static float[][] transpose(float[][] input) {
        float[][] output = new float[input[0].length][input.length];
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                output[j][i] = input[i][j];
            }
        }
        return output;
    }
    public static double[][] transpose(double[][] input) {
        double[][] output = new double[input[0].length][input.length];
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                output[j][i] = input[i][j];
            }
        }
        return output;
    }

    public static float[][] transpose(float[][] input, int length) {
        float[][] output = new float[input[0].length][length];
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                output[j][i] = input[i][j];
            }
        }
        return output;
    }

    public static float[][] transposeSingle(float[] input, int length) {
        float[][] output = new float[input.length][length];
        for (int i = 0; i < input.length; i++) {
            output[i][0] = input[i];
        }
        return output;
    }
}
