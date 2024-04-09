package com.crypto.analysis.main.core.data_utils.normalizers.robust;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class RobustScaler implements Serializable {
    private transient final Percentile percentile = new Percentile();

    private final HashMap<double[][], double[]> median;
    private final HashMap<double[][], double[]> iqr;

    @Getter
    @Setter
    private HashMap<Integer, ArrayList<Double>> volatileData;
    private double[] volatileMedian;
    private double[] volatileIQR;

    private final int[] mask;
    @Setter
    private int countInputs;
    @Setter
    private int countOutputs;
    private final int notVolatileLength;
    private final int volatileLength;

    private final int sequenceLength;
    private boolean isInitialized;

    private boolean hasChanged;

    public RobustScaler(int[] mask, int notVolatileLength, int volatileLength) {
        this.mask = mask;
        this.notVolatileLength = notVolatileLength;
        this.volatileLength = volatileLength;

        this.sequenceLength = volatileLength + notVolatileLength;
        median = new HashMap<>();
        iqr = new HashMap<>();
    }


    public void fit(LinkedList<double[][]> data) {
        if (data == null || data.isEmpty()) throw new IllegalArgumentException("Input list cannot be empty");
        if (data.get(0).length == 0 || data.get(0)[0].length == 0)
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

        double[] median = new double[notVolatileLength];
        double[] iqr = new double[notVolatileLength];

        for (int i = 0; i < notVolatileLength; i++) {
            double[] column = getColumn(input, i, countInputs);
            median[i] = calculateMedian(column);
            iqr[i] = calculateIQR(column);
        }

        this.median.put(input, median);
        this.iqr.put(input, iqr);

        if (!isInitialized) isInitialized = true;
        hasChanged = true;
    }

    public void transform(LinkedList<double[][]> inputList) {
        if (inputList == null || inputList.isEmpty() || inputList.get(0).length == 0 || inputList.get(0)[0].length == 0)
            throw new IllegalArgumentException("Input list cannot be empty");

        for (double[][] input : inputList) {
            transform(input);
        }
    }

    public void transform(double[][] input) {
        if (volatileData.size() != volatileLength)
            throw new IllegalStateException("Volatile data map size are not equals to volatile params count");

        double[] rMedian = this.median.get(input);
        double[] rIqr = this.iqr.get(input);

        double[] vMedian = calculateMediansVolatileData();
        double[] vIqr = calculateIQRsVolatileData();

        double[] median = new double[rMedian.length + vMedian.length];
        System.arraycopy(rMedian, 0, median, 0, rMedian.length);
        System.arraycopy(vMedian, 0, median, rMedian.length, vMedian.length);

        double[] iqr = new double[rIqr.length + vIqr.length];
        System.arraycopy(rIqr, 0, iqr, 0, rIqr.length);
        System.arraycopy(vIqr, 0, iqr, rIqr.length, vIqr.length);

        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                input[i][j] = (input[i][j] - median[j]) / iqr[j];
            }
        }
        hasChanged = false;
    }

    public void revertFeatures(double[][] input) {
        if (input == null || input.length == 0 || input[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");
        if (!median.containsKey(input) || !iqr.containsKey(input))
            throw new IllegalArgumentException("Normalizer doesnt has stats for this array");
        if (volatileData.size() != volatileLength)
            throw new IllegalStateException("Volatile data map size are not equals to volatile params count");

        double[] rMedian = this.median.get(input);
        double[] rIqr = this.iqr.get(input);

        double[] vMedian = calculateMediansVolatileData();
        double[] vIqr = calculateIQRsVolatileData();

        double[] median = new double[rMedian.length + vMedian.length];
        System.arraycopy(rMedian, 0, median, 0, rMedian.length);
        System.arraycopy(vMedian, 0, median, rMedian.length, vMedian.length);

        double[] iqr = new double[rIqr.length + vIqr.length];
        System.arraycopy(rIqr, 0, iqr, 0, rIqr.length);
        System.arraycopy(vIqr, 0, iqr, rIqr.length, vIqr.length);

        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                input[i][j] = (input[i][j] * iqr[i]) + median[i];
            }
        }
    }

    public void revertLabels(double[][] key, double[][] input) {
        if (input == null || input.length == 0 || input[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");
        if (key == null || key.length == 0)
            throw new IllegalArgumentException("Key array cannot be empty");
        if (!median.containsKey(key) || !iqr.containsKey(key))
            throw new IllegalArgumentException("Normalizer doesnt has stats for this array");
        if (volatileData.size() != volatileLength)
            throw new IllegalStateException("Volatile data map size are not equals to volatile params count");

        double[] rMedian = this.median.get(key);
        double[] rIqr = this.iqr.get(key);

        double[] vMedian = calculateMediansVolatileData();
        double[] vIqr = calculateIQRsVolatileData();

        double[] median = new double[rMedian.length + vMedian.length];
        System.arraycopy(rMedian, 0, median, 0, rMedian.length);
        System.arraycopy(vMedian, 0, median, rMedian.length, vMedian.length);

        double[] iqr = new double[rIqr.length + vIqr.length];
        System.arraycopy(rIqr, 0, iqr, 0, rIqr.length);
        System.arraycopy(vIqr, 0, iqr, rIqr.length, vIqr.length);

        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < countOutputs; j++) {
                input[i][j] = (input[i][j] * iqr[mask[i]]) + median[mask[i]];
            }
        }
    }

    public void changeBinding(double[][] original, double[][] newBinding) {
        if (!this.median.containsKey(original) || !this.iqr.containsKey(original))
            throw new IllegalArgumentException("Normalizer doesnt has stats for this array");

        double[] median = this.median.get(original);
        double[] iqr = this.iqr.get(original);

        this.median.remove(original);
        this.iqr.remove(original);

        this.median.put(newBinding, median);
        this.iqr.put(newBinding, iqr);
    }

    private double[] calculateMediansVolatileData() {
        if (volatileData == null || volatileData.isEmpty()) throw new IllegalStateException();
        if (volatileData.size() != volatileLength)
            throw new IllegalStateException("Volatile data map size are not equals to volatile params count");

        if (!hasChanged) return volatileMedian;
        double[] median = new double[volatileLength];
        int index = 0;
        for (int i = notVolatileLength; i < sequenceLength; i++) {
            Double[] arrayVolatile = volatileData.get(i).toArray(new Double[0]);
            double[] converted = new double[arrayVolatile.length];
            for (int j = 0; j < arrayVolatile.length; j++) {
                converted[j] = arrayVolatile[j];
            }
            median[index++] = calculateMedian(converted);
        }
        this.volatileMedian = median;
        return median;
    }

    private double[] calculateIQRsVolatileData() {
        if (volatileData == null || volatileData.isEmpty()) throw new IllegalStateException();
        if (volatileData.size() != volatileLength)
            throw new IllegalStateException("Volatile data map size are not equals to volatile params count");

        if (!hasChanged) return volatileIQR;
        double[] iqr = new double[volatileLength];
        int index = 0;
        for (int i = notVolatileLength; i < sequenceLength; i++) {
            Double[] arrayVolatile = volatileData.get(i).toArray(new Double[0]);
            double[] converted = new double[arrayVolatile.length];
            for (int j = 0; j < arrayVolatile.length; j++) {
                converted[j] = arrayVolatile[j];
            }
            iqr[index++] = calculateIQR(converted);
        }
        this.volatileIQR = iqr;
        return iqr;
    }

    private double[] getColumn(double[][] input, int columnIndex, int lastIndex) {
        if (lastIndex > input.length) throw new IllegalArgumentException("Last index must be less than array length");
        double[] column = new double[lastIndex];
        for (int i = 0; i < lastIndex; i++) {
            column[i] = input[i][columnIndex];
        }
        return column;
    }

    private double calculateMedian(double[] column) {
        return percentile.evaluate(column, 50);
    }

    private double calculateIQR(double[] column) {
        double q1 = percentile.evaluate(column, 25);
        double q3 = percentile.evaluate(column, 75);
        return q3 - q1;
    }
}
