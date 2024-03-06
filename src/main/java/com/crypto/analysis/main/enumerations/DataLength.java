package com.crypto.analysis.main.enumerations;

import lombok.Getter;

@Getter
public enum DataLength {
    D30_5 (30, 5),
    D70_10 (70, 10),
    D100_15 (100, 15);

    public static final int MAX_INPUT_LENGTH = 100;
    public static final int MAX_OUTPUT_LENGTH = 15;

    private final int countInput;
    private final int countOutput;

    DataLength(int countInput, int countOutput) {
        this.countInput = countInput;
        this.countOutput = countOutput;
    }
}
