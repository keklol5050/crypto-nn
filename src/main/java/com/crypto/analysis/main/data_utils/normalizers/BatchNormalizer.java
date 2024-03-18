package com.crypto.analysis.main.data_utils.normalizers;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class BatchNormalizer implements Serializable {
    private final LinkedHashMap<double[][], double[]> min = new LinkedHashMap<>();
    private final LinkedHashMap<double[][], double[]> max = new LinkedHashMap<>();
    private final double epsilon = Math.random() / 1000;

    private final int[] mask;
    private final int countInputs;
    private final int countOutputs;

    public BatchNormalizer(int[] mask, int countInputs, int countOutputs) {
        this.mask = mask;
        this.countInputs = countInputs;
        this.countOutputs = countOutputs;
        System.out.println("Epsilon " + epsilon);
    }

    public void fitHorizontal(LinkedList<double[][]> data) {
        if (data == null || data.isEmpty()) throw new IllegalArgumentException("Input list cannot be empty");
        if (data.get(0).length == 0 || data.get(0)[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");

        for (double[][] dataArr : data) {
            fitHorizontal(dataArr);
        }
    }

    public void fitHorizontal(double[][] data) {
        if (data.length == 0 || data[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");

        int paramCapacity = data[0].length;

        double[] min = new double[paramCapacity];
        double[] max = new double[paramCapacity];

        double[][] reverted = new double[countInputs][];
        System.arraycopy(data, 0, reverted, 0, countInputs);

        for (int i = 0; i < paramCapacity; i++) {
            double valueMin = Double.MAX_VALUE;
            double valueMax = Double.MIN_VALUE;

            for (double[] datum : reverted) {
                if (datum.length != paramCapacity)
                    throw new IllegalArgumentException("Arrays parameters are not equals");

                valueMin = Math.min(valueMin, datum[i]);
                valueMax = Math.max(valueMax, datum[i]);
            }

            if (valueMax - valueMin == 0) {
                valueMax += epsilon;
            }

            min[i] = valueMin;
            max[i] = valueMax;
        }

        this.min.put(data, min);
        this.max.put(data, max);
    }

    public void transformHorizontal(LinkedList<double[][]> inputList) {
        if (inputList == null || inputList.isEmpty() || inputList.get(0).length == 0 || inputList.get(0)[0].length == 0)
            throw new IllegalArgumentException("Input list cannot be empty");

        for (double[][] input : inputList) {
            transformHorizontal(input);
        }
    }

    public void transformHorizontal(double[][] input) {
        if (input == null || input.length == 0 || input[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");
        if (!min.containsKey(input) || !max.containsKey(input))
            throw new IllegalArgumentException("Normalizer doesnt has stats for this array");

        double[] min = this.min.get(input);
        double[] max = this.max.get(input);

        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                input[i][j] = ((input[i][j] - min[j]) / (max[j] - min[j]));
            }
        }
    }

    public void transformVertical(LinkedList<double[][]> inputList) {
        if (inputList == null || inputList.isEmpty() || inputList.get(0).length == 0 || inputList.get(0)[0].length == 0)
            throw new IllegalArgumentException("Input list cannot be empty");

        for (double[][] input : inputList) {
            transformVertical(input);
        }
    }

    public void transformVertical(double[][] input) {
        if (input == null || input.length == 0 || input[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");
        if (!min.containsKey(input) || !max.containsKey(input))
            throw new IllegalArgumentException("Normalizer doesnt has stats for this array");

        double[] min = this.min.get(input);
        double[] max = this.max.get(input);

        for (int i = 0; i < input[0].length; i++) {
            for (int j = 0; j < input.length; j++) {
                input[j][i] = ((input[j][i] - min[i]) / (max[i] - min[i]));
            }
        }
    }

    public void revertFeaturesVertical(double[][] input) {
        if (input == null || input.length == 0 || input[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");
        if (!min.containsKey(input) || !max.containsKey(input))
            throw new IllegalArgumentException("Normalizer doesnt has stats for this array");

        double[] min = this.min.get(input);
        double[] max = this.max.get(input);

        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                input[i][j] = (input[i][j]) * (max[i] - min[i]) + min[i];
            }
        }
    }

    public void revertLabelsVertical(double[][] key, double[][] input) {
        if (input == null || input.length == 0 || input[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");
        if (key==null || key.length == 0)
            throw new IllegalArgumentException("Key array cannot be empty");
        if (!min.containsKey(key) || !max.containsKey(key))
            throw new IllegalArgumentException("Normalizer doesnt has stats for this array");

        double[] min = this.min.get(key);
        double[] max = this.max.get(key);

        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < countOutputs; j++) {
                input[i][j] = (input[i][j]) * (max[mask[i]] - min[mask[i]]) + min[mask[i]];
            }
        }
    }

    public void changeBinding(double[][] original, double[][] newBinding) {
        if (!this.min.containsKey(original) || !this.max.containsKey(original))
            throw new IllegalArgumentException("Normalizer doesnt has stats for this array");

        double[] min = this.min.get(original);
        double[] max = this.max.get(original);

        this.min.remove(original);
        this.max.remove(original);

        this.min.put(newBinding, min);
        this.max.put(newBinding, max);
    }

    public void saveNormalizer(String basePath) throws Exception {
        if (basePath == null || basePath.isEmpty()) throw new NullPointerException();
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(basePath));
        out.writeObject(this);
        System.out.println("Normalizer saved on the path: " + basePath);
    }

    public static BatchNormalizer loadNormalizer(String basePath) throws Exception {
        if (basePath == null || basePath.isEmpty()) throw new NullPointerException();
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(basePath));
        return (BatchNormalizer) in.readObject();
    }
}
