package com.crypto.analysis.main.data_utils;

import com.crypto.analysis.main.enumerations.Periods;
import com.crypto.analysis.main.vo.CandleObject;
import com.crypto.analysis.main.vo.DataObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

import static com.crypto.analysis.main.data_utils.BinanceDataUtil.client;

public class BinanceDataMultipleInstance {
    public static DataObject[] getLatestInstances(String symbol, Periods interval) throws JsonProcessingException {
        DataObject[] instances = new DataObject[30];
        LinkedList<CandleObject> candles = BinanceDataUtil.getCandles(symbol, interval, 30);

        IndicatorsDataUtil util = new IndicatorsDataUtil(symbol, interval);

        int count = candles.size();
        for (int i = 0; i < count; i++) {
            DataObject obj = new DataObject(symbol, interval);
            obj.setCurrentIndicators(util.getIndicators(candles.size() - 1));
            CandleObject candle = candles.removeFirst();
            obj.setCandle(candle);
            instances[i] = obj;
        }
        return instances;
    }

    public static void main(String[] args) throws JsonProcessingException {
        System.out.println(
                Arrays.toString(BinanceDataMultipleInstance.getLatestInstances("BTCUSDT", Periods.ONE_HOUR)));
    }
}
