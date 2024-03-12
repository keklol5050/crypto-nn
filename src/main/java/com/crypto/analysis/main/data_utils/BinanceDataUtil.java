package com.crypto.analysis.main.data_utils;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.crypto.analysis.main.enumerations.Coin;
import com.crypto.analysis.main.enumerations.TimeFrame;
import com.crypto.analysis.main.vo.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class BinanceDataUtil {
    public static final UMFuturesClientImpl client = new UMFuturesClientImpl();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private Coin coin; // наприклад "BTCUSDT"
    private TimeFrame interval; // 1m 3m 5m 15m 30m 1h 2h 4h 6h 8h 12h 1d 3d 1w 1M

    public BinanceDataUtil(Coin coin, TimeFrame interval) { // !! формат типу "BTCUSDT" "15m" 4
        this.coin = coin;
        this.interval = interval;
    }

    public static LinkedList<CandleObject> getCandles(Coin coin, TimeFrame interval, int capacity) {
        LinkedList<CandleObject> result = new LinkedList<>();
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", coin.getName());
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
                    Double.parseDouble(candlestick.get(5).toString()), new Date((long) candlestick.get(6)));
            result.add(candleObject);
        }
        return result;
    }

    public static FundingHistoryObject getFundingHistory(Coin coin) {
        TreeMap<Date, Double> resultMap = new TreeMap<>();
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", coin.getName());
        parameters.put("limit", 1000);
        String funding = client.market().fundingRate(parameters);
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(funding);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        for (JsonNode node : jsonNode) {
            long fundingTime = node.get("fundingTime").asLong();
            double fundingRate = node.get("fundingRate").asDouble();
            Date date = new Date(fundingTime);
            resultMap.put(date, fundingRate);
        }
        return new FundingHistoryObject(resultMap);
    }

    public static OpenInterestHistoryObject getOpenInterest(Coin coin, TimeFrame period) {
        TreeMap<Date, Double> resultMap = new TreeMap<>();

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", coin.getName());
        parameters.put("period", period.getTimeFrame());
        parameters.put("limit", 500);
        String openInterest = client.market().openInterestStatistics(parameters);

        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(openInterest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        for (JsonNode node : jsonNode) {
            long timestamp = node.get("timestamp").asLong();
            double sumOpenInterest = node.get("sumOpenInterest").asDouble();
            Date date = new Date(timestamp);
            resultMap.put(date, sumOpenInterest);
        }

        return new OpenInterestHistoryObject(resultMap);
    }

    public static LongShortRatioHistoryObject getLongShortRatio(Coin coin, TimeFrame period) {
        TreeMap<Date, Double> resultMap = new TreeMap<>();

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", coin.getName());
        parameters.put("period", period.getTimeFrame());
        parameters.put("limit", 500);
        String longShortStat = client.market().longShortRatio(parameters);

        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(longShortStat);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        for (JsonNode node : jsonNode) {
            long timestamp = node.get("timestamp").asLong();
            double longShortRatio = node.get("longShortRatio").asDouble();
            Date date = new Date(timestamp);
            resultMap.put(date, longShortRatio);
        }

        return new LongShortRatioHistoryObject(resultMap);
    }

    public static BuySellRatioHistoryObject getBuySellRatio(Coin coin, TimeFrame period) {
        TreeMap<Date, Double> resultMap = new TreeMap<>();

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", coin.getName());
        parameters.put("period", period.getTimeFrame());
        parameters.put("limit", 500);
        String buySellVolume = client.market().takerBuySellVol(parameters);

        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(buySellVolume);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        for (JsonNode node : jsonNode) {
            long timestamp = node.get("timestamp").asLong();
            double buySellRatio = node.get("buySellRatio").asDouble();
            Date date = new Date(timestamp);
            resultMap.put(date, buySellRatio);
        }

        return new BuySellRatioHistoryObject(resultMap);
    }


    public static double getCurrentPrice(Coin coin) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", coin.getName());
        return Double.parseDouble(new UMFuturesClientImpl().market().markPrice(parameters));
    }

    public DataObject getSingleInstance() {
        DataObject[] instances = BinanceDataMultipleInstance.getLatestInstances(coin, interval, 1);
        return instances[instances.length - 1];
    }

    public static void main(String[] args) throws JsonProcessingException {
        LinkedList<Double> result = new LinkedList<>();

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", Coin.BTCUSDT.getName());
        parameters.put("period", TimeFrame.FIVE_MINUTES.getTimeFrame());
        parameters.put("limit", 30);
        String buySellVol = client.market().openInterest(parameters);
        System.out.println(buySellVol);
        System.out.println(BinanceDataUtil.getOpenInterest(Coin.BTCUSDT, TimeFrame.FIVE_MINUTES));
        System.out.println(BinanceDataUtil.getBuySellRatio(Coin.BTCUSDT, TimeFrame.FIVE_MINUTES));
        System.out.println(BinanceDataUtil.getLongShortRatio(Coin.BTCUSDT, TimeFrame.FIVE_MINUTES));
    }
}
