package com.crypto.analysis.main.data_utils;

import com.crypto.analysis.main.enumerations.Periods;
import com.crypto.analysis.main.vo.CandleObject;
import com.crypto.analysis.main.vo.DataObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static com.crypto.analysis.main.data_utils.BinanceDataUtil.client;

public class BinanceDataMultipleInstance {
    public static DataObject[] getLatestInstances(String symbol, Periods interval) throws JsonProcessingException {
        DataObject[] instances = new DataObject[10];
        LinkedList<CandleObject> candles = BinanceDataUtil.getCandles(symbol, interval, 10);

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        parameters.put("period", interval.getTimeFrame());
        parameters.put("limit", 10);

        List<LinkedList<Double>> params = BinanceDataMultipleInstance.setParameters(parameters, interval);
        int count = candles.size();
        for (int i = 0; i < count; i++) {
            DataObject obj = new DataObject(symbol, interval);
            obj.setCurrentIndicators(IndicatorsDataUtil.getIndicators(candles, candles.size() - 1));
            obj.setCandle(candles.removeFirst());
            obj.setShortRatio(params.get(2).removeFirst());
            obj.setLongRatio(params.get(1).removeFirst());
            obj.setCurrentOpenInterest(params.get(0).removeFirst());
            obj.setCurrentFundingRate(params.get(3).getFirst());

            instances[i] = obj;
        }
        return instances;
    }

    public static List<LinkedList<Double>> setParameters(LinkedHashMap<String, Object> parameters, Periods interval) throws JsonProcessingException {
        LinkedList<Double> openInterestList = new LinkedList<>();
        LinkedList<Double> longRatio = new LinkedList<>();
        LinkedList<Double> shortRatio = new LinkedList<>();
        LinkedList<Double> fundingList = new LinkedList<>();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(client.market().openInterestStatistics(parameters));
        for (JsonNode node : rootNode) {
            double sumOpenInterest = node.get("sumOpenInterest").asDouble();
            openInterestList.add(sumOpenInterest);
        }

        rootNode = mapper.readTree(client.market().longShortRatio(parameters));
        for (JsonNode node : rootNode) {
            double lRatio = node.get("longAccount").asDouble();
            double sRatio = node.get("shortAccount").asDouble();
            longRatio.add(lRatio);
            shortRatio.add(sRatio);
        }

        rootNode = mapper.readTree(client.market().fundingRate(parameters));
        for (JsonNode node : rootNode) {
            double funding = node.get("fundingRate").asDouble();
            fundingList.add(funding);
        }
        return List.of(openInterestList, longRatio, shortRatio, fundingList);
    }

    public static void main(String[] args) throws JsonProcessingException {
        System.out.println(
                Arrays.toString(BinanceDataMultipleInstance.getLatestInstances("BTCUSDT", Periods.ONE_HOUR)));
    }
}
