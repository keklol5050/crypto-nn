package com.crypto.analysis.main.fundamental.stock.enumerations;

import lombok.Getter;

@Getter
public enum FundamentalStock {
    SPX("SPX"),
    DXY("DXY"),
    DJI("DJI"),
    VIX("VIX"),
    NDX("NDX"),
    GOLD("XAU/USD");

    private final String name;
    FundamentalStock(String name) {
        this.name = name;
    }

}
