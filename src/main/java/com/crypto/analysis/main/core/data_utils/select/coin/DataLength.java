package com.crypto.analysis.main.core.data_utils.select.coin;

import lombok.Getter;

import static com.crypto.analysis.main.core.data_utils.select.StaticData.NUMBER_OF_DIFFERENTIATIONS;

@Getter
public enum DataLength {
    S30_3(30+NUMBER_OF_DIFFERENTIATIONS, 3),
    L60_6(60+NUMBER_OF_DIFFERENTIATIONS, 6),
    X100_10(100+NUMBER_OF_DIFFERENTIATIONS, 10),
    CLASSIFICATION(30,3),
    VOLATILITY_REGRESSION(60, 6);

    public static final int MIN_INPUT_LENGTH = 30+NUMBER_OF_DIFFERENTIATIONS;
    public static final int MIN_OUTPUT_LENGTH = 3;

    public static final int MAX_INPUT_LENGTH = 100+NUMBER_OF_DIFFERENTIATIONS;
    public static final int MAX_OUTPUT_LENGTH = 10;

    private final int countInput;
    private final int countOutput;

    DataLength(int countInput, int countOutput) {
        this.countInput = countInput;
        this.countOutput = countOutput;
    }
}
