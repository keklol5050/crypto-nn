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

    private final LinkedList<DataObject> trainData = new LinkedList<>();
    private final LinkedList<Double> trainResult = new LinkedList<>();

    private final LinkedList<DataObject> testData = new LinkedList<>();
    private final LinkedList<Double> testResult = new LinkedList<>();

    public TrainData(String symbol, Periods interval){
        this.symbol = symbol;
        this.interval = interval;
        init();
    }

    private void init() {
       try {
           candles = BinanceDataUtil.getCandles(symbol, interval, 1440);
           if (candles.size()<601) {
               while ((candles.size()-1)%30!=0 || (((candles.size()-1)/3)%30!=0)) {
                   candles.removeFirst();
               }
           }
           int countMax = candles.size()-1;
           int countMain = countMax - countMax/6;


           LinkedList<DataObject> localTrainData = new LinkedList<>();
           LinkedList<Double> localTrainResult = new LinkedList<>();

           LinkedList<DataObject> localTestData = new LinkedList<>();
           LinkedList<Double> localTestResult = new LinkedList<>();

           for (int i = 1; i<candles.size(); i++) {
               if (i < countMain+1) {
                   localTrainResult.add(candles.get(i).getClose()/10000);
               } else localTestResult.add(candles.get(i).getClose()/10000);
           }
           candles.removeLast();

           IndicatorsDataUtil util = new IndicatorsDataUtil(symbol, interval);

           for (int i = 0; i < countMax; i++) {
               DataObject obj = new DataObject(symbol, interval);
               obj.setCurrentIndicators(util.getIndicators(candles.size()-1));
               CandleObject candle = candles.removeFirst();
               obj.setCandle(candle);
              if (i < countMain) {
                  localTrainData.add(obj);
              } else localTestData.add(obj);
           }

           trainData.addAll(localTrainData);
           trainResult.addAll(localTrainResult);

           testData.addAll(localTestData);
           testResult.addAll(localTestResult);
       } catch (Exception e) {
           throw new RuntimeException(e);
       }
    }

    public void updateInterval(Periods interval) {
        this.interval = interval;
        init();
    }
    public static void main(String[] args) {
        TrainData train = new TrainData("BTCUSDT", Periods.ONE_MONTH);
        System.out.println(train.getTrainData());
        System.out.println(train.getTrainResult());
        System.out.println(train.getTestData());
        System.out.println(train.getTestResult());
    }
}
