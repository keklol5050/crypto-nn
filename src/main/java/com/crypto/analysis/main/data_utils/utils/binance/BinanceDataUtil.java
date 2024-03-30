package com.crypto.analysis.main.data_utils.utils.binance;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.crypto.analysis.main.data_utils.select.coin.Coin;
import com.crypto.analysis.main.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.vo.CandleObject;
import com.crypto.analysis.main.vo.indication.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

import static com.crypto.analysis.main.data_utils.select.StaticData.client;
import static com.crypto.analysis.main.data_utils.select.StaticData.objectMapper;

public class BinanceDataUtil {

    public static LinkedList<CandleObject> getCandles(Coin coin, TimeFrame interval, int capacity) {
        LinkedList<CandleObject> result = new LinkedList<>();
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", coin.getName());
        parameters.put("interval", interval.getTimeFrame());
        parameters.put("limit", capacity);
        String candles = client.market().klines(parameters);
        List<List<Object>> candlestickList = null;
        try {
            candlestickList = objectMapper.readValue(candles, new TypeReference<>() {
            });
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

    public static BTCDOMObject getBTCDomination(TimeFrame interval) {
        LinkedList<CandleObject> candlesDOM = getCandles(Coin.BTCDOMUSDT, interval, 1500);
        TreeMap<Date, Double> resultMap = new TreeMap<Date, Double>();
        for (CandleObject candle : candlesDOM) {
            resultMap.put(candle.getOpenTime(), candle.getOpen());
        }
        return new BTCDOMObject(resultMap);
    }

    public static double getCurrentPrice(Coin coin) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", coin.getName());
        return Double.parseDouble(new UMFuturesClientImpl().market().markPrice(parameters));
    }
}
