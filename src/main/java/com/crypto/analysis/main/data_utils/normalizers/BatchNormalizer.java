package com.crypto.analysis.main.data_utils.normalizers;

import com.crypto.analysis.main.data_utils.select.StaticData;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class BatchNormalizer implements Serializable {
    private final LinkedHashMap<double[][], double[]> min = new LinkedHashMap<>();
    private final LinkedHashMap<double[][], double[]> max = new LinkedHashMap<>();

    private double[] volatileMin;
    private double[] volatileMax;
    private final double epsilon = Math.random() / 10000;

    private final int[] mask;
    private final int countInputs;
    private final int countOutputs;
    private final int notVolatileLength;
    public BatchNormalizer(int[] mask, int countInputs, int countOutputs, int notVolatileLength) {
        this.mask = mask;
        this.countInputs = countInputs;
        this.countOutputs = countOutputs;
        this.notVolatileLength = notVolatileLength;

        init();
    }

    private void init() {
        this.volatileMin = new double[StaticData.VOLATILE_VALUES_COUNT_FROM_LAST];
        this.volatileMax = new double[StaticData.VOLATILE_VALUES_COUNT_FROM_LAST];

        for (int i = 0; i < StaticData.VOLATILE_VALUES_COUNT_FROM_LAST; i++) {
            volatileMin[i] = Long.MAX_VALUE;
            volatileMax[i] = Long.MIN_VALUE;
        }

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
            double valueMin = Long.MAX_VALUE;
            double valueMax = Long.MIN_VALUE;

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

        double[] dMin = new double[notVolatileLength];
        double[] dMax = new double[notVolatileLength];

        System.arraycopy(min, 0, dMin, 0, dMin.length);
        System.arraycopy(max, 0, dMax, 0, dMax.length);

        double[] vMin = new double[paramCapacity-notVolatileLength];
        double[] vMax = new double[paramCapacity-notVolatileLength];

        System.arraycopy(min, notVolatileLength, vMin, 0, vMin.length);
        System.arraycopy(max, notVolatileLength, vMax, 0, vMax.length);

        for (int i = 0; i < vMin.length; i++) {
            this.volatileMin[i] = Math.min(vMin[i], volatileMin[i]);
            this.volatileMax[i] = Math.max(vMax[i], volatileMax[i]);
        }

        this.min.put(data, dMin);
        this.max.put(data, dMax);
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

        double[] tMin = this.min.get(input);
        double[] tMax = this.max.get(input);

        double[] min = new double[tMin.length+volatileMin.length];
        System.arraycopy(tMin, 0, min, 0, tMin.length);
        System.arraycopy(volatileMin, 0, min, tMin.length, volatileMin.length);

        double[] max = new double[tMax.length+volatileMax.length];
        System.arraycopy(tMax, 0, max, 0, tMax.length);
        System.arraycopy(volatileMax, 0, max, tMax.length, volatileMax.length);

        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                input[i][j] = ((input[i][j] - min[j]) / (max[j] - min[j]));
            }
        }
    }

    public void revertFeaturesVertical(double[][] input) {
        if (input == null || input.length == 0 || input[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");
        if (!min.containsKey(input) || !max.containsKey(input))
            throw new IllegalArgumentException("Normalizer doesnt has stats for this array");

        double[] tMin = this.min.get(input);
        double[] tMax = this.max.get(input);

        double[] min = new double[tMin.length+volatileMin.length];
        System.arraycopy(tMin, 0, min, 0, tMin.length);
        System.arraycopy(volatileMin, 0, min, tMin.length, volatileMin.length);

        double[] max = new double[tMax.length+volatileMax.length];
        System.arraycopy(tMax, 0, max, 0, tMax.length);
        System.arraycopy(volatileMax, 0, max, tMax.length, volatileMax.length);

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

        double[] tMin = this.min.get(key);
        double[] tMax = this.max.get(key);

        double[] min = new double[tMin.length+volatileMin.length];
        System.arraycopy(tMin, 0, min, 0, tMin.length);
        System.arraycopy(volatileMin, 0, min, tMin.length, volatileMin.length);

        double[] max = new double[tMax.length+volatileMax.length];
        System.arraycopy(tMax, 0, max, 0, tMax.length);
        System.arraycopy(volatileMax, 0, max, tMax.length, volatileMax.length);

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
