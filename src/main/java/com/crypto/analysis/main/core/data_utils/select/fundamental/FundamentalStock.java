package com.crypto.analysis.main.core.data_utils.select.fundamental;

import lombok.Getter;

@Getter
public enum FundamentalStock {
    SPX("SPX"),
    DXY("DXY"),
    DJI("DJI"),
    VIX("VIX"),
    NDX("NDX"),
    GOLD("XAUUSD");

    private final String name;

    FundamentalStock(String name) {
        this.name = name;
    }

}
