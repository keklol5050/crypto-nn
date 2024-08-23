package com.crypto.analysis.main.core.data_utils.normalizers;

import com.crypto.analysis.main.core.data_utils.utils.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

public class MaxAbsScaler {
    private static final float SCALE_FACTOR = PropertiesUtil.getPropertyAsFloat("normalizer.scale_factor");

    private final HashMap<float[][], float[]> cache = new HashMap<>();
    private final int[] mask;
    private final int countOutputs;
    private final int sequenceLength;

    private static final Logger logger = LoggerFactory.getLogger(MaxAbsScaler.class);

    public MaxAbsScaler(int[] mask, int countOutputs, int sequenceLength) {
        this.mask = mask;
        this.countOutputs = countOutputs;
        this.sequenceLength = sequenceLength;

        logger.info("MaxAbsScaler created with scaleFactor={}", SCALE_FACTOR);
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

            if (Float.isNaN(maxValue) || Float.isInfinite(maxValue)) {
                throw new IllegalStateException();
            }

            max[i] = maxValue;
        }

        this.cache.put(input, max);
    }

    public void transform(ArrayList<float[][]> inputList, boolean clear) {
        if (inputList == null || inputList.isEmpty() || inputList.getFirst().length == 0 || inputList.getFirst()[0].length == 0)
            throw new IllegalArgumentException("Input list cannot be empty");

        for (float[][] input : inputList) {
            transform(input, clear);
        }
    }

    public void transform(float[][] input, boolean clear) {
        if (input == null || input.length == 0 || input[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");
        if (!cache.containsKey(input))
            throw new IllegalArgumentException("Normalizer doesnt has stats for this array");
        if (sequenceLength != input[0].length)
            throw new IllegalArgumentException("Input array length are not equals");

        float[] maxAbsValues = this.cache.get(input);

        for (int i = 0; i < sequenceLength; i++) {
            for (int j = 0; j < input.length; j++) {
                float value = (input[j][i] / maxAbsValues[i]) * SCALE_FACTOR;

                if (Float.isNaN(value) || Float.isInfinite(value)) {
                    throw new ArithmeticException("Value is NaN");
                }

                input[j][i] = value;
            }
        }

        if (clear) {
            this.cache.remove(input);
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
                input[j][i] = (input[j][i] / SCALE_FACTOR) * maxAbsValues[i];
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
                input[j][i] = (input[j][i] / SCALE_FACTOR) * maxAbsValues[mask[i]];
            }
        }
    }

    public float maxAbsValue(float[] data) {
        float maxAbsValue = 0;
        for (float num : data) {
            maxAbsValue = Math.max(maxAbsValue, Math.abs(num));
        }
        return maxAbsValue;
    }
}
