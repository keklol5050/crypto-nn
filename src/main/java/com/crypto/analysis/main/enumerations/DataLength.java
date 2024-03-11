package com.crypto.analysis.main.enumerations;

import lombok.Getter;

@Getter
public enum DataLength {
    S50_3(30, 3),
    L100_6(70, 7),
    X150_9(100, 10);

    public static final int MAX_INPUT_LENGTH = 100;
    public static final int MAX_OUTPUT_LENGTH = 10;

    private final int countInput;
    private final int countOutput;

    DataLength(int countInput, int countOutput) {
        this.countInput = countInput;
        this.countOutput = countOutput;
    }
}
