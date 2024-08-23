package com.crypto.analysis.main.core.data_utils.select.coin;

import lombok.Getter;

@Getter
public enum Coin {
    BTCUSDT("BTCUSDT", new String[]{"f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8"}),
    ETHUSDT("ETHUSDT", new String[]{"f21", "f22", "f32", "f14", "f15", "f16", "f27", "f28"}),
    BTCDOMUSDT("BTCDOMUSDT", new String[]{});

    private final String name;
    private final String[] fundCols;

    Coin(String name, String[] fundCols) {
        this.name = name;
        this.fundCols = fundCols;
    }
}
