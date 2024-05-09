package com.crypto.analysis.main.core.data_utils.normalizers;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.HashMap;

public class StandardizeNormalizer {
    private final HashMap<double[][], double[]> standardDeviation = new HashMap<>();
    private final HashMap<double[][], double[]> mean = new HashMap<>();

    private final int[] mask;
    private final int countOutputs;
    private final int sequenceLength;

    public StandardizeNormalizer(int[] mask, int countOutputs, int sequenceLength) {
        this.mask = mask;
        this.countOutputs = countOutputs;
        this.sequenceLength = sequenceLength;
    }

    public void fit(ArrayList<double[][]> data) {
        if (data == null || data.isEmpty()) throw new IllegalArgumentException("Input list cannot be empty");
        if (data.getFirst().length == 0 || data.getFirst()[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");

        for (double[][] dataArr : data) {
            fit(dataArr);
        }
    }

    public void fit(double[][] input) {
        if (input.length == 0 || input[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");
        if (sequenceLength != input[0].length)
            throw new IllegalArgumentException("Input array length are not equals");

        double[] standardDeviation = new double[sequenceLength];
        double[] mean = new double[sequenceLength];

        for (int i = 0; i < sequenceLength; i++) {
            DescriptiveStatistics stats = new DescriptiveStatistics();
            for (int j = 0; j < input.length; j++) {
                stats.addValue(input[j][i]);
            }

            double stdDevValue = stats.getStandardDeviation();
            double meanValue = stats.getMean();

            if (Double.isNaN(stdDevValue) || Double.isInfinite(stdDevValue)){
                System.out.println("stdDevValue is NaN " + stdDevValue);
            }
            if (Double.isNaN(meanValue) || Double.isInfinite(meanValue)){
                System.out.println("meanValue is NaN " + meanValue);
            }

            standardDeviation[i] = stdDevValue;
            mean[i] = meanValue;
        }

        this.standardDeviation.put(input, standardDeviation);
        this.mean.put(input, mean);
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
        if (!standardDeviation.containsKey(input) || !mean.containsKey(input))
            throw new IllegalArgumentException("Normalizer doesnt has stats for this array");
        if (sequenceLength != input[0].length)
            throw new IllegalArgumentException("Input array length are not equals");

        double[] standardDeviation = this.standardDeviation.get(input);
        double[] mean = this.mean.get(input);

        for (int i = 0; i < sequenceLength; i++) {
            for (int j = 0; j < input.length; j++) {
                double value = (input[j][i] - mean[i]) / standardDeviation[i];
                if (Math.abs(value) > 10) {
                    System.out.println("Value " + value);
                }
                if (Double.isNaN(value) || Double.isInfinite(value)){
                    System.out.println("Value NaN " + value);
                }
                input[j][i] = value;
            }
        }
    }

    public void revertFeatures(double[][] input) {
        if (input == null || input.length == 0 || input[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");
        if (!standardDeviation.containsKey(input) || !mean.containsKey(input))
            throw new IllegalArgumentException("Normalizer doesnt has stats for this array");
        if (sequenceLength != input[0].length)
            throw new IllegalArgumentException("Input array length are not equals");

        double[] standardDeviation = this.standardDeviation.get(input);
        double[] mean = this.mean.get(input);

        for (int i = 0; i < sequenceLength; i++) {
            for (int j = 0; j < input.length; j++) {
                input[j][i] = (input[j][i] * standardDeviation[i]) + mean[i];
            }
        }
    }

    public void revertLabels(double[][] key, double[][] input) {
        if (input == null || input.length == 0 || input[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");
        if (key == null || key.length == 0)
            throw new IllegalArgumentException("Key array cannot be empty");
        if (!standardDeviation.containsKey(key) || !mean.containsKey(key))
            throw new IllegalArgumentException("Normalizer doesnt has stats for this array");

        double[] standardDeviation = this.standardDeviation.get(key);
        double[] mean = this.mean.get(key);

        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < countOutputs; j++) {
                input[j][i] = (input[j][i] * standardDeviation[mask[i]]) + mean[mask[i]];
            }
        }
    }

    public void changeBinding(double[][] original, double[][] newBinding) {
        if (!this.standardDeviation.containsKey(original) || !this.mean.containsKey(original))
            throw new IllegalArgumentException("Normalizer doesnt has stats for this array");

        double[] standardDeviation = this.standardDeviation.get(original);
        double[] mean = this.mean.get(original);

        this.standardDeviation.remove(original);
        this.mean.remove(original);

        this.standardDeviation.put(newBinding, standardDeviation);
        this.mean.put(newBinding, mean);
    }
}
