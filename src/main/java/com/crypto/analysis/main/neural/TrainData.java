package com.crypto.analysis.main.neural;

import com.crypto.analysis.main.data_utils.*;
import com.crypto.analysis.main.enumerations.Periods;
import com.crypto.analysis.main.vo.CandleObject;
import com.crypto.analysis.main.vo.DataObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.util.*;

@Getter
public class TrainData {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final String symbol;
    private Periods interval;
    private LinkedList<CandleObject> candles;

    private final List<DataObject> trainData = new ArrayList<>();
    private final List<Double> trainResult = new ArrayList<>();


    public TrainData(String symbol, Periods interval) throws JsonProcessingException {
        this.symbol = symbol;
        this.interval = interval;
        init();
    }

    private void init() {
       try {
           candles = BinanceDataUtil.getCandles(symbol, interval, 1201);
           for (int i = 1; i<candles.size(); i++) {
               trainResult.add(candles.get(i).getClose()/10000);
           }
           candles.removeLast();

           IndicatorsDataUtil util = new IndicatorsDataUtil(symbol, interval);

           int count = candles.size();

           for (int i = 0; i < count; i++) {
               DataObject obj = new DataObject(symbol, interval);
               obj.setCurrentIndicators(util.getIndicators(candles.size()-1));
               CandleObject candle = candles.removeFirst();
               obj.setCandle(candle);
               trainData.add(obj);
           }
       } catch (Exception e) {
           throw new RuntimeException(e);
       }
    }

    public void updateInterval(Periods interval) {
        this.interval = interval;
        init();
    }
    public static void main(String[] args) throws JsonProcessingException {
        TrainData t = new TrainData("BTCUSDT", Periods.ONE_HOUR);
        System.out.println(t.getTrainData());
        System.out.println(t.getTrainResult());
    }
}
