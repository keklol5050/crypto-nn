package com.crypto.analysis.main.vo;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import java.util.LinkedHashMap;

public class DataObject {
    public static void main(String[] args) {
        UMFuturesClientImpl client = new UMFuturesClientImpl();
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", "BTCUSDT");
        parameters.put("interval", "15m");
        parameters.put("limit", "60");
        String result = client.market().klines(parameters);
        System.out.println(result);

    }
}
