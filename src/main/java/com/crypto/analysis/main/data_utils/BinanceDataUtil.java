package com.crypto.analysis.main.data_utils;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.crypto.analysis.main.vo.CandleObject;
import com.crypto.analysis.main.vo.DataObject;
import com.crypto.analysis.main.vo.Ticker24Object;
import com.crypto.analysis.main.vo.TickerBookObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

@Getter
@Setter
public class BinanceDataUtil {
    private static final UMFuturesClientImpl client = new UMFuturesClientImpl();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private String symbol; // наприклад "BTCUSDT"
    private String interval; // 1m 3m 5m 15m 30m 1h 2h 4h 6h 8h 12h 1d 3d 1w 1M
    private int capacity; // кількість свічок

    public static void main(String[] args) {
        BinanceDataUtil bdu = new BinanceDataUtil("BTCUSDT", "15m", 4);
        DataObject obj = bdu.getInstance();
        System.out.println(obj);
    }


    public BinanceDataUtil(String symbol, String interval, int capacity) { // !! формат типу "BTCUSDT" "15m" 4
        this.symbol = symbol;
        this.interval = interval;
        this.capacity = capacity;
    }

    public DataObject getInstance() {
        DataObject obj = new DataObject(symbol);
        obj.setCandles(getCandles());
        setFuturesData(obj);
        IndicatorsDataUtil indicatorsDataUtil = new IndicatorsDataUtil(symbol, interval);
        obj.setCurrentIndicators(indicatorsDataUtil.getLatestIndicatorsInfo());
        return obj;
    }

    public List<CandleObject> getCandles() {
        List<CandleObject> result = new ArrayList<>();
         LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("symbol", symbol);
            parameters.put("interval", interval);
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

    public void setFuturesData(DataObject object) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        parameters.put("period", interval);
        parameters.put("limit", capacity);
        try {
            setLongShortRatio(object, parameters);
            setFundingAndOpenInterest(object, parameters);
            setTopTradersLongShortRatio(object, parameters);
            setTakerSellBuyVolume(object, parameters);
            setTickerBookObject(object);
            set24HTicker(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace(System.out);
        }
    }

    private void setLongShortRatio(DataObject object, LinkedHashMap<String, Object> parameters) throws JsonProcessingException {
        String longShortRatio = client.market().longShortRatio(parameters);
        double longRatio = objectMapper.readTree(longShortRatio).get(0).get("longAccount").asDouble();
        double shortRatio = objectMapper.readTree(longShortRatio).get(0).get("shortAccount").asDouble();

        object.setCurrentLongShortRatio(String.format("%f=%f", longRatio, shortRatio));
    }

    private void setFundingAndOpenInterest(DataObject object, LinkedHashMap<String, Object> parameters) throws JsonProcessingException {
        double openInterest = objectMapper.readTree(client.market().openInterest(parameters)).get("openInterest").asDouble();
        double fundingRate = objectMapper.readTree(client.market().fundingRate(parameters)).get(0).get("fundingRate").asDouble();

        object.setCurrentFundingRate(fundingRate);
        object.setCurrentOpenInterest(openInterest);
    }

    private void setTopTradersLongShortRatio(DataObject object, LinkedHashMap<String,Object> parameters) throws JsonProcessingException {
        String topTraderLongShortRatio = client.market().topTraderLongShortPos(parameters);
        double longRatio = objectMapper.readTree(topTraderLongShortRatio).get(0).get("longAccount").asDouble();
        double shortRatio = objectMapper.readTree(topTraderLongShortRatio).get(0).get("shortAccount").asDouble();
        object.setCurrentTopTradersLongShortRatio(String.format("%f=%f", longRatio, shortRatio));
    }

    private void setTakerSellBuyVolume(DataObject object, LinkedHashMap<String,Object> parameters) throws JsonProcessingException {
        String topTraderLongShortRatio = client.market().takerBuySellVol(parameters);
        double buySellRatio = objectMapper.readTree(topTraderLongShortRatio).get(0).get("buySellRatio").asDouble();
        double sellVol = objectMapper.readTree(topTraderLongShortRatio).get(0).get("sellVol").asDouble();
        double buyVol = objectMapper.readTree(topTraderLongShortRatio).get(0).get("buyVol").asDouble();

        object.setCurrentBuySellRatioAndVolumes(String.format("%f=%f-%f", buySellRatio,buyVol, sellVol));
    }

    private void setTickerBookObject(DataObject object) throws JsonProcessingException {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", "BTCUSDT");
        parameters.put("limit", 1000);
        String bookTicker = client.market().bookTicker(parameters);
        TickerBookObject tickerBookObject = objectMapper.readValue(bookTicker, TickerBookObject.class);

        object.setTickerBookObject(tickerBookObject);
    }

    private void set24HTicker(DataObject object) throws JsonProcessingException {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", "BTCUSDT");
        String bookTicker = client.market().ticker24H(parameters);
        Ticker24Object ticker24Object = objectMapper.readValue(bookTicker, Ticker24Object.class);

        object.setTicker24Object(ticker24Object);
    }
    public static double getCurrentPrice(String symbol) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        return Double.parseDouble(new UMFuturesClientImpl().market().markPrice(parameters));
    }
}
