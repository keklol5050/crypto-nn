package com.crypto.analysis.main.core.data_utils.select.coin;

import lombok.Getter;

@Getter
public enum Coin {
    BTCUSDT("BTCUSDT"),
    ETHUSDT("ETHUSDT"),
    SOLUSDT("SOLUSDT"),
    BTCDOMUSDT("BTCDOMUSDT");

    private final String name;

    Coin(String name) {
        this.name = name;
    }
}
