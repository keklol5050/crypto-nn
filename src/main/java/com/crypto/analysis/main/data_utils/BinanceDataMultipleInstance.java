package com.crypto.analysis.main.data_utils;

import com.crypto.analysis.main.enumerations.TimeFrame;
import com.crypto.analysis.main.vo.CandleObject;
import com.crypto.analysis.main.vo.DataObject;

import java.util.Arrays;
import java.util.LinkedList;

public class BinanceDataMultipleInstance {
    public static DataObject[] getLatestInstances(String symbol, TimeFrame interval) {
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

    public static void main(String[] args) {
        System.out.println(
                Arrays.toString(BinanceDataMultipleInstance.getLatestInstances("BTCUSDT", TimeFrame.ONE_DAY)));
    }
}
