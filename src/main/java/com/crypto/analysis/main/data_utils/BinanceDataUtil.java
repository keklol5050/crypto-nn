package com.crypto.analysis.main.data_utils;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.crypto.analysis.main.enumerations.Periods;
import com.crypto.analysis.main.vo.CandleObject;
import com.crypto.analysis.main.vo.DataObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class BinanceDataUtil {
    public static final UMFuturesClientImpl client = new UMFuturesClientImpl();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private String symbol; // наприклад "BTCUSDT"
    private Periods interval; // 1m 3m 5m 15m 30m 1h 2h 4h 6h 8h 12h 1d 3d 1w 1M

    public static void main(String[] args) {
       LinkedList<CandleObject> dO = getCandles("BTCUSDT", Periods.ONE_MONTH, 1500);
        System.out.println(dO);
        System.out.println(dO.size());
    }


    public BinanceDataUtil(String symbol, Periods interval) { // !! формат типу "BTCUSDT" "15m" 4
        this.symbol = symbol;
        this.interval = interval;
    }

    public DataObject getSingleInstance() {
        DataObject obj = new DataObject(symbol, interval);
        IndicatorSingleDataUtil indicatorSingleDataUtil = new IndicatorSingleDataUtil(symbol, interval);
        obj.setCurrentIndicators(indicatorSingleDataUtil.getIndicatorsInfo());
        obj.setCandle(indicatorSingleDataUtil.getLastCandle());
        return obj;
    }

    public static LinkedList<CandleObject> getCandles(String symbol, Periods interval, int capacity) {
        LinkedList<CandleObject> result = new LinkedList<>();
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        parameters.put("interval", interval.getTimeFrame());
        parameters.put("limit", capacity);
        String candles = client.market().klines(parameters);
        List<List<Object>> candlestickList = null;
        try {
            candlestickList = objectMapper.readValue(candles, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace(System.out);
        }
        assert candlestickList != null;
        for (List<Object> candlestick : candlestickList) {
            CandleObject candleObject = new CandleObject(
                    new Date((Long) candlestick.get(0)),
                    Double.parseDouble(candlestick.get(1).toString()), Double.parseDouble(candlestick.get(2).toString()),
                    Double.parseDouble(candlestick.get(3).toString()), Double.parseDouble(candlestick.get(4).toString()),
                    Double.parseDouble(candlestick.get(5).toString()), new Date((long)candlestick.get(6)),
                    Double.parseDouble(candlestick.get(7).toString()), (int) candlestick.get(8),  Double.parseDouble(candlestick.get(9).toString()),
                    Double.parseDouble(candlestick.get(10).toString()));
            result.add(candleObject);
        }
        return result;
    }


    public static double getCurrentPrice(String symbol) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        return Double.parseDouble(new UMFuturesClientImpl().market().markPrice(parameters));
    }
}
