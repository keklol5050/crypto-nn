package com.crypto.analysis.main.core.data_utils.utils;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.fundamental.crypto.BitQueryUtil;
import com.crypto.analysis.main.core.updater.CoinUpdater;
import com.crypto.analysis.main.core.vo.CandleObject;
import com.crypto.analysis.main.core.vo.DataObject;
import com.crypto.analysis.main.core.vo.indication.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import kotlin.Pair;

import java.io.IOException;
import java.util.*;

import static com.crypto.analysis.main.core.data_utils.select.StaticUtils.client;
import static com.crypto.analysis.main.core.data_utils.select.StaticUtils.objectMapper;

public class BinanceDataUtil {

    public static ArrayList<CandleObject> getCandles(Coin coin, TimeFrame interval, int capacity) {
        ArrayList<CandleObject> result = new ArrayList<>();
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
                    Float.parseFloat(candlestick.get(1).toString()), Float.parseFloat(candlestick.get(2).toString()),
                    Float.parseFloat(candlestick.get(3).toString()), Float.parseFloat(candlestick.get(4).toString()),
                    Float.parseFloat(candlestick.get(5).toString()), new Date((long) candlestick.get(6)));
            result.add(candleObject);
        }
        return result;
    }

    public static FundingHistoryObject getFundingHistory(Coin coin) {
        TreeMap<Date, Float> resultMap = new TreeMap<>();
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
            float fundingRate = (float) node.get("fundingRate").asDouble();
            Date date = new Date(fundingTime);
            resultMap.put(date, fundingRate);
        }
        return new FundingHistoryObject(resultMap);
    }

    public static OpenInterestHistoryObject getOpenInterest(Coin coin, TimeFrame period) {
        TreeMap<Date, Float> resultMap = new TreeMap<>();

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
            float sumOpenInterest = (float) node.get("sumOpenInterest").asDouble();
            Date date = new Date(timestamp);
            resultMap.put(date, sumOpenInterest);
        }

        return new OpenInterestHistoryObject(resultMap);
    }

    public static LongShortRatioHistoryObject getLongShortRatio(Coin coin, TimeFrame period) {
        TreeMap<Date, Float> resultMap = new TreeMap<>();

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
            float longShortRatio = (float) node.get("longShortRatio").asDouble();
            Date date = new Date(timestamp);
            resultMap.put(date, longShortRatio);
        }

        return new LongShortRatioHistoryObject(resultMap);
    }

    public static BTCDOMObject getBTCDomination(TimeFrame interval) {
        ArrayList<CandleObject> candlesDOM = getCandles(Coin.BTCDOMUSDT, interval, 1500);
        TreeMap<Date, float[]> resultMap = new TreeMap<Date, float[]>();
        for (CandleObject candle : candlesDOM) {
            resultMap.put(candle.getOpenTime(), candle.getValuesArr());
        }
        return new BTCDOMObject(resultMap);
    }

    public static double getCurrentPrice(Coin coin) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", coin.getName());
        return Float.parseFloat(new UMFuturesClientImpl().market().markPrice(parameters));
    }

    public static DataObject[] getLatestInstances(Coin coin, TimeFrame interval, int count) {
        DataObject[] instances = new DataObject[count];
        ArrayList<CandleObject> candles = BinanceDataUtil.getCandles(coin, interval, count);

        IndicatorsDataUtil util = new IndicatorsDataUtil(coin, interval);
        FundingHistoryObject funding = BinanceDataUtil.getFundingHistory(coin);

        LongShortRatioHistoryObject longShortRatioHistoryObject = BinanceDataUtil.getLongShortRatio(coin, interval);
        OpenInterestHistoryObject openInterest = BinanceDataUtil.getOpenInterest(coin, interval);

        BTCDOMObject BTCDom = BinanceDataUtil.getBTCDomination(interval);
        SentimentHistoryObject sentiment = SentimentUtil.getData();

        BitQueryUtil bitQueryUtil = new BitQueryUtil(coin, interval);
        bitQueryUtil.initData(candles.getFirst().getOpenTime(), candles.getLast().getCloseTime());

        TreeMap<Date, Pair<Float, Float>> metricsMap = null;

        for (int i = 0; i < count; i++) {
            DataObject obj = new DataObject(coin, interval);
            obj.setCurrentIndicators(util.getIndicators(candles.size() - 1));

            CandleObject candle = candles.removeFirst();
            obj.setCandle(candle);

            obj.setCurrentFundingRate(funding.getValueForNearestDate(candle.getOpenTime()));

            float oiValue;
            float lsrRatio;

            try {
                oiValue = openInterest.getValueForNearestDate(candle.getOpenTime());
                lsrRatio = longShortRatioHistoryObject.getValueForNearestDate(candle.getOpenTime());
            } catch (Exception e) {
                    try {
                        if (metricsMap == null)
                             metricsMap = CoinUpdater.getMetricsMap(coin);
                        oiValue = metricsMap.get(candle.getOpenTime()).getFirst();
                        lsrRatio = metricsMap.get(candle.getOpenTime()).getSecond();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
            }

            obj.setCurrentOpenInterest(oiValue);
            obj.setLongShortRatio(lsrRatio);

            obj.setBTCDomination(BTCDom.getValueForNearestDate(candle.getOpenTime()));
            float[] sentValues = sentiment.getValueForNearestDate(candle.getOpenTime());
            obj.setSentimentMean(sentValues[0]);
            obj.setSentimentSum(sentValues[1]);

            obj.setCryptoFundamental(bitQueryUtil.getData(candle));

            instances[i] = obj;
        }

        return instances;
    }

    public static void main(String[] args) {
        OpenInterestHistoryObject oi = getOpenInterest(Coin.BTCUSDT, TimeFrame.FIFTEEN_MINUTES);
        for (Map.Entry<Date, Float> entry : oi.getMap().entrySet()){
            System.out.printf("%s,%s\n", entry.getKey(), entry.getValue());
        }
    }

}
