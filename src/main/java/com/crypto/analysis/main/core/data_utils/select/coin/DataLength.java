package com.crypto.analysis.main.core.data_utils.select.coin;

import lombok.Getter;

@Getter
public enum DataLength {
    S100_5(100, 5),
    L120_6(120, 6),
    X180_9(180, 9),
    XL240_12(240, 12),
    CUSTOM(-1,-1);

    public static final int MIN_REG_INPUT_LENGTH = 100;
    public static final int MIN_REG_OUTPUT_LENGTH = 5;

    public static final int MAX_REG_INPUT_LENGTH = 240;
    public static final int MAX_REG_OUTPUT_LENGTH = 12;

    public static final DataLength MAX_LENGTH = XL240_12;

    private int countInput;
    private int countOutput;

    DataLength(int countInput, int countOutput) {
        this.countInput = countInput;
        this.countOutput = countOutput;
    }

    public void setCountInput(int countInput) {
        if (this != CUSTOM)
            throw new IllegalArgumentException("Cannot set countInput for non-custom DataLength");

        this.countInput = countInput;
    }

    public void setCountOutput(int countOutput) {
        if (this!= CUSTOM)
            throw new IllegalArgumentException("Cannot set countOutput for non-custom DataLength");

        this.countOutput = countOutput;
    }
}
