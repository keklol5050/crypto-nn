package com.crypto.analysis.main.enumerations;

import lombok.Getter;

@Getter
public enum Coin {
    BTCUSDT("BTCUSDT"),
    ETHUSDT("ETHUSDT"),;

    private final String name;
    Coin(String name) {
        this.name = name;
    }
}
