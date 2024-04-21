package com.crypto.analysis.main.core.vo;

import lombok.Getter;

import java.util.Arrays;

@Getter
public class TrainSetElement {
    private final double[][] data;
    private final double[][] result;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrainSetElement that)) return false;
        return Arrays.deepEquals(getData(), that.getData()) && Arrays.deepEquals(getResult(), that.getResult());
    }

    @Override
    public int hashCode() {
        int result1 = Arrays.deepHashCode(getData());
        result1 = 31 * result1 + Arrays.deepHashCode(getResult());
        return result1;
    }
}
