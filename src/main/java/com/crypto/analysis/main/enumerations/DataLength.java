package com.crypto.analysis.main.enumerations;

import lombok.Getter;

@Getter
public enum DataLength {
    S30_3(24, 1),
    L70_7(70, 7),
    X100_10(100, 10);

    public static final int MAX_INPUT_LENGTH = 100;
    public static final int MAX_OUTPUT_LENGTH = 10;

    private final int countInput;
    private final int countOutput;

    DataLength(int countInput, int countOutput) {
        this.countInput = countInput;
        this.countOutput = countOutput;
    }
}
