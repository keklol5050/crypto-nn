package com.crypto.analysis.main.data_utils.select.coin;

import lombok.Getter;

@Getter
public enum DataLength {
    S50_3(50, 3),
    L70_6(70, 6),
    X100_9(100, 9);

    public static final int MAX_INPUT_LENGTH = 100;
    public static final int MAX_OUTPUT_LENGTH = 9;

    private final int countInput;
    private final int countOutput;

    DataLength(int countInput, int countOutput) {
        this.countInput = countInput;
        this.countOutput = countOutput;
    }
}
