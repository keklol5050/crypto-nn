package com.crypto.analysis.main.vo;

import java.util.Arrays;


public class TrainSetElement {
    private double[][] matrixData;
    private double[] arrayData;
    private double[][] resultMatrix;
    private double[] resultArray;

    public TrainSetElement(double[][] data, double[][] result) {
        this.matrixData = data;
        this.resultMatrix = result;
    }

    public TrainSetElement(double[] data, double[] result) {
        this.arrayData = data;
        this.resultArray = result;
    }

    public double[][] getDataMatrix() {
        if (matrixData == null) throw new IllegalStateException("Data matrix is not initialized, try to switch single data array");
        return matrixData;
    }

    public double[] getDataArray() {
        if (arrayData == null) throw new IllegalStateException("Array data value is not initialized, try to switch data matrix");
        return arrayData;
    }

    public double[][] getResultMatrix() {
        if (resultMatrix == null) throw new IllegalStateException("Result matrix is not initialized, try to switch single result array");
        return resultMatrix;
    }

    public double[] getResultArray() {
        if (resultArray == null) throw new IllegalStateException("Array result value is not initialized, try to switch result matrix");
        return resultArray;
    }

    @Override
    public String toString() {
        return "TrainSetElement{" +
                "data=" + (matrixData!=null ? Arrays.deepToString(matrixData) : Arrays.toString(arrayData)) +
                ", result=" + (resultMatrix!=null ? Arrays.deepToString(resultMatrix) : Arrays.toString(resultArray)) +
                '}';
    }
}
