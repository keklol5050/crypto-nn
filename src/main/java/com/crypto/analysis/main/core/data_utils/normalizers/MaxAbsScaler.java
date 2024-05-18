package com.crypto.analysis.main.core.data_utils.normalizers;

import java.util.ArrayList;
import java.util.HashMap;

public class MaxAbsScaler {
    private final HashMap<double[][], double[]> cache = new HashMap<>();

    private final int[] mask;
    private final int countOutputs;
    private final int sequenceLength;


    public MaxAbsScaler(int[] mask, int countOutputs, int sequenceLength) {
        this.mask = mask;
        this.countOutputs = countOutputs;
        this.sequenceLength = sequenceLength;
    }

    public void fit(ArrayList<double[][]> data, int countInput) {
        if (data == null || data.isEmpty()) throw new IllegalArgumentException("Input list cannot be empty");
        if (data.getFirst().length == 0 || data.getFirst()[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");

        for (double[][] dataArr : data) {
            fit(dataArr, countInput);
        }
    }

    public void fit(double[][] input, int countInput) {
        if (input.length == 0 || input[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");
        if (sequenceLength != input[0].length)
            throw new IllegalArgumentException("Input array length are not equals");

        double[] max = new double[sequenceLength];

        for (int i = 0; i < sequenceLength; i++) {
            double[] internalArray = new double[countInput];

            for (int j = 0; j < internalArray.length; j++) {
                internalArray[j] = input[j][i];
            }

            double maxValue = maxAbsValue(internalArray);

            if (Double.isNaN(maxValue) || Double.isInfinite(maxValue)) {
                throw new IllegalStateException();
            }

            max[i] = maxValue;
        }

        this.cache.put(input, max);
    }

    public void transform(ArrayList<double[][]> inputList) {
        if (inputList == null || inputList.isEmpty() || inputList.getFirst().length == 0 || inputList.getFirst()[0].length == 0)
            throw new IllegalArgumentException("Input list cannot be empty");

        for (double[][] input : inputList) {
            transform(input);
        }
    }

    public void transform(double[][] input) {
        if (input == null || input.length == 0 || input[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");
        if (!cache.containsKey(input))
            throw new IllegalArgumentException("Normalizer doesnt has stats for this array");
        if (sequenceLength != input[0].length)
            throw new IllegalArgumentException("Input array length are not equals");

        double[] maxAbsValues = this.cache.get(input);

        for (int i = 0; i < sequenceLength; i++) {
            for (int j = 0; j < input.length; j++) {
                double value = input[j][i] / maxAbsValues[i];

                if (Double.isNaN(value) || Double.isInfinite(value)) {
                    throw new ArithmeticException("Value is NaN");
                }

                input[j][i] = value;
            }
        }
    }

    public void revertFeatures(double[][] input) {
        if (input == null || input.length == 0 || input[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");
        if (!cache.containsKey(input))
            throw new IllegalArgumentException("Normalizer doesnt has stats for this array");
        if (sequenceLength != input[0].length)
            throw new IllegalArgumentException("Input array length are not equals");

        double[] maxAbsValues = this.cache.get(input);

        for (int i = 0; i < sequenceLength; i++) {
            for (int j = 0; j < input.length; j++) {
                input[j][i] = input[j][i] * maxAbsValues[i];
            }
        }
    }

    public void revertLabels(double[][] key, double[][] input) {
        if (input == null || input.length == 0 || input[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");
        if (key == null || key.length == 0)
            throw new IllegalArgumentException("Key array cannot be empty");
        if (!cache.containsKey(key))
            throw new IllegalArgumentException("Normalizer doesnt has stats for this array");

        double[] maxAbsValues = this.cache.get(key);

        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < countOutputs; j++) {
                input[j][i] = input[j][i] * maxAbsValues[mask[i]];
            }
        }
    }

    public void changeBinding(double[][] original, double[][] newBinding) {
        if (!this.cache.containsKey(original))
            throw new IllegalArgumentException("Normalizer doesnt has stats for this array");

        double[] max = this.cache.get(original);

        this.cache.remove(original);

        this.cache.put(newBinding, max);
    }

    public double maxAbsValue(double[] data) {
        double maxAbsValue = 0;
        for (double num : data) {
            maxAbsValue = Math.max(maxAbsValue, Math.abs(num));
        }
        return maxAbsValue;
    }
}
