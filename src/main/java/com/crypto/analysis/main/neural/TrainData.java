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
           int capacity = switch (interval) {
               case FIVE_MINUTES, FIFTEEN_MINUTES, THIRTY_MINUTES, ONE_HOUR -> 500;
               case TWO_HOURS -> 360;
               case FOUR_HOURS -> 180;
               case SIX_HOURS -> 120;
               case TWELVE_HOURS -> 60;
               case ONE_DAY -> 30;
           };
           candles = BinanceDataUtil.getCandles(symbol, interval, capacity+1);
           for (int i = 1; i<candles.size(); i++) {
               trainResult.add(candles.get(i).getClose()/10000);
           }
           candles.removeLast();
           LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
           parameters.put("symbol", symbol);
           parameters.put("period", interval.getTimeFrame());
           parameters.put("limit", capacity);

           List<LinkedList<Double>> params = BinanceDataMultipleInstance.setParameters(parameters);
           TreeMap<Date, Double> fundingMap = BinanceDataMultipleInstance.getFundingMap(parameters);

           int count = Math.min(params.get(0).size(), params.get(1).size());
           IndicatorsDataUtil util = new IndicatorsDataUtil(symbol, interval);
           for (int i = 0; i < count; i++) {
               DataObject obj = new DataObject(symbol, interval);
               obj.setCurrentIndicators(util.getIndicators(candles.size()-1));
               CandleObject candle = candles.removeFirst();
               obj.setCandle(candle);
               obj.setCurrentOpenInterest(params.get(0).removeFirst());
               obj.setLongRatio(params.get(1).removeFirst());
               obj.setShortRatio(params.get(2).removeFirst());
               obj.setCurrentFundingRate(ClosestDateFinder.findClosestValue(fundingMap,candle.getCloseTime()));
               trainData.add(obj);
           }
           if (params.get(0).isEmpty() && params.get(1).isEmpty() && params.get(2).isEmpty()) {
               System.out.println("no errors");
           } else
               System.out.println("were errors");
       } catch (Exception e) {
           throw new RuntimeException(e);
       }
    }



    public static void main(String[] args) throws JsonProcessingException {
        TrainData t = new TrainData("BTCUSDT", Periods.ONE_HOUR);
        System.out.println(t.getTrainData());
        System.out.println(t.getTrainResult());
    }

    public void updateInterval(Periods interval) {
        this.interval = interval;
        init();
    }
}
