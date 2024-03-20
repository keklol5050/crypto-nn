package com.crypto.analysis.main.data_utils.select.coin;

import lombok.Getter;

@Getter
public enum DataLength {
    S50_3(50, 3),
    L100_6(100, 6),
    X150_9(150, 9);

    public static final int MAX_INPUT_LENGTH = 150;
    public static final int MAX_OUTPUT_LENGTH = 9;

    private final int countInput;
    private final int countOutput;

    DataLength(int countInput, int countOutput) {
        this.countInput = countInput;
        this.countOutput = countOutput;
    }
}
