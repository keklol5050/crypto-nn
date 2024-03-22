package com.crypto.analysis.main.vo;

import lombok.Getter;

import java.util.Arrays;

@Getter
public class TrainSetElement {
    private double[][] data;
    private double[][] result;

    public TrainSetElement(double[][] data, double[][] result) {
        this.data = data;
        this.result = result;
    }

    @Override
    public String toString() {
        return "TrainSetElement{" +
                "data=" + Arrays.deepToString(data) +
                ", result=" + Arrays.deepToString(result) +
                '}';
    }
}
