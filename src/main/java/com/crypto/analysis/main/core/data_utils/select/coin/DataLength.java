package com.crypto.analysis.main.core.data_utils.select.coin;

import lombok.Getter;

import static com.crypto.analysis.main.core.data_utils.select.StaticData.NUMBER_OF_DIFFERENTIATIONS;

@Getter
public enum DataLength {
    S60_3(60+NUMBER_OF_DIFFERENTIATIONS, 3),
    L120_6(120+NUMBER_OF_DIFFERENTIATIONS, 6),
    X180_9(180+NUMBER_OF_DIFFERENTIATIONS, 9),
    CLASSIFICATION(30, 3),
    VOLATILITY_REGRESSION(60, 6);

    public static final int MIN_REG_INPUT_LENGTH = 60+NUMBER_OF_DIFFERENTIATIONS;
    public static final int MIN_REG_OUTPUT_LENGTH = 3;

    public static final int MAX_REG_INPUT_LENGTH = 180+NUMBER_OF_DIFFERENTIATIONS;
    public static final int MAX_REG_OUTPUT_LENGTH = 9;

    private final int countInput;
    private final int countOutput;

    DataLength(int countInput, int countOutput) {
        this.countInput = countInput;
        this.countOutput = countOutput;
    }
}
