package com.crypto.analysis.main.core.data_utils.normalizers;

import java.util.ArrayList;
import java.util.HashMap;

public class MaxAbsScaler {
    private final HashMap<float[][], float[]> cache = new HashMap<>();

    private final int[] mask;
    private final int countOutputs;
    private final int sequenceLength;


    public MaxAbsScaler(int[] mask, int countOutputs, int sequenceLength) {
        this.mask = mask;
        this.countOutputs = countOutputs;
        this.sequenceLength = sequenceLength;
    }

    public void fit(ArrayList<float[][]> data, int countInput) {
        if (data == null || data.isEmpty()) throw new IllegalArgumentException("Input list cannot be empty");
        if (data.getFirst().length == 0 || data.getFirst()[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");

        for (float[][] dataArr : data) {
            fit(dataArr, countInput);
        }
    }

    public void fit(float[][] input, int countInput) {
        if (input.length == 0 || input[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");
        if (sequenceLength != input[0].length)
            throw new IllegalArgumentException("Input array length are not equals");

        float[] max = new float[sequenceLength];

        for (int i = 0; i < sequenceLength; i++) {
            float[] internalArray = new float[countInput];

            for (int j = 0; j < internalArray.length; j++) {
                internalArray[j] = input[j][i];
            }

            float maxValue = maxAbsValue(internalArray);

            if (Double.isNaN(maxValue) || Double.isInfinite(maxValue)) {
                throw new IllegalStateException();
            }

            max[i] = maxValue;
        }

        this.cache.put(input, max);
    }

    public void transform(ArrayList<float[][]> inputList) {
        if (inputList == null || inputList.isEmpty() || inputList.getFirst().length == 0 || inputList.getFirst()[0].length == 0)
            throw new IllegalArgumentException("Input list cannot be empty");

        for (float[][] input : inputList) {
            transform(input);
        }
    }

    public void transform(float[][] input) {
        if (input == null || input.length == 0 || input[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");
        if (!cache.containsKey(input))
            throw new IllegalArgumentException("Normalizer doesnt has stats for this array");
        if (sequenceLength != input[0].length)
            throw new IllegalArgumentException("Input array length are not equals");

        float[] maxAbsValues = this.cache.get(input);

        for (int i = 0; i < sequenceLength; i++) {
            for (int j = 0; j < input.length; j++) {
                float value = input[j][i] / maxAbsValues[i];

                if (Double.isNaN(value) || Double.isInfinite(value)) {
                    throw new ArithmeticException("Value is NaN");
                }

                input[j][i] = value;
            }
        }
    }

    public void revertFeatures(float[][] input) {
        if (input == null || input.length == 0 || input[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");
        if (!cache.containsKey(input))
            throw new IllegalArgumentException("Normalizer doesnt has stats for this array");
        if (sequenceLength != input[0].length)
            throw new IllegalArgumentException("Input array length are not equals");

        float[] maxAbsValues = this.cache.get(input);

        for (int i = 0; i < sequenceLength; i++) {
            for (int j = 0; j < input.length; j++) {
                input[j][i] = input[j][i] * maxAbsValues[i];
            }
        }
    }

    public void revertLabels(float[][] key, float[][] input) {
        if (input == null || input.length == 0 || input[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");
        if (key == null || key.length == 0)
            throw new IllegalArgumentException("Key array cannot be empty");
        if (!cache.containsKey(key))
            throw new IllegalArgumentException("Normalizer doesnt has stats for this array");

        float[] maxAbsValues = this.cache.get(key);

        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < countOutputs; j++) {
                input[j][i] = input[j][i] * maxAbsValues[mask[i]];
            }
        }
    }

    public void changeBinding(float[][] original, float[][] newBinding) {
        if (!this.cache.containsKey(original))
            throw new IllegalArgumentException("Normalizer doesnt has stats for this array");

        float[] max = this.cache.get(original);

        this.cache.remove(original);

        this.cache.put(newBinding, max);
    }

    public float maxAbsValue(float[] data) {
        float maxAbsValue = 0;
        for (float num : data) {
            maxAbsValue = Math.max(maxAbsValue, Math.abs(num));
        }
        return maxAbsValue;
    }
}
