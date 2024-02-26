package com.crypto.analysis.main.data_utils;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.crypto.analysis.main.vo.CandleObject;
import com.crypto.analysis.main.vo.DataObject;
import com.crypto.analysis.main.vo.IndicatorsTransferObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.volume.OnBalanceVolumeIndicator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Getter
public class TrainData {
    private static final UMFuturesClientImpl client = new UMFuturesClientImpl();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final String symbol;
    private final String interval;
    private List<CandleObject> candles;


    private DataObject[] trainData;


    public TrainData(String symbol, String interval) throws JsonProcessingException {
        this.symbol = symbol;
        this.interval = interval;
        init();
    }
    private void init() throws JsonProcessingException {
        BinanceDataUtil binanceData = new BinanceDataUtil(symbol, "5m", 1500);
        candles = binanceData.getCandles();

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        parameters.put("period", "15m");
        parameters.put("limit", 500);

        ObjectMapper mapper = new ObjectMapper();

        JsonNode rootNode = mapper.readTree(client.market().openInterestStatistics(parameters));
        LinkedList<Double> openInterestList = new LinkedList<>();
        for (JsonNode node : rootNode) {
            double sumOpenInterest = node.get("sumOpenInterest").asDouble();
            openInterestList.add(sumOpenInterest);
        }

        rootNode = mapper.readTree(client.market().longShortRatio(parameters));
        LinkedList<Double> longRatio = new LinkedList<>();
        LinkedList<Double> shortRatio = new LinkedList<>();
        for (JsonNode node : rootNode) {
            double lRatio = node.get("longAccount").asDouble();
            double sRatio = node.get("shortAccount").asDouble();
            longRatio.add(lRatio);
            shortRatio.add(sRatio);
        }

        trainData = new DataObject[candles.size()/3];
        for (int i = 0; i < trainData.length; i++) {
            DataObject obj = new DataObject(symbol);
            LinkedList<CandleObject> thisCandles = new LinkedList<>();
            obj.setCurrentIndicators(getIndicators(candles.size()-1));
            for (int j = 0; j < 3; j++) {
                thisCandles.add(candles.remove(candles.size()-1));
            }
            obj.setCandles(thisCandles);
            obj.setCurrentOpenInterest(openInterestList.removeLast()*10000);
            obj.setLongRatio(longRatio.removeLast());
            obj.setShortRatio(shortRatio.removeLast());
            trainData[i] = obj;
        }
        System.out.println(openInterestList.size());
        System.out.println(longRatio.size());
        System.out.println(shortRatio.size());
    }

    public List<Double> getTrainResult() {
        return Arrays.stream(trainData).map(DataObject::getResultCandleCloseValue).toList();
    }


    private IndicatorsTransferObject getIndicators(int countBar) {
        TimeSeries series = IndicatorsDataUtil.getTimeSeries(candles);

        IndicatorsTransferObject result = new IndicatorsTransferObject();

        RSIIndicator rsiIndicator = new RSIIndicator(new ClosePriceIndicator(series), 14);
        result.setRSI(rsiIndicator.getValue(countBar).doubleValue());

        MACDIndicator macd = new MACDIndicator(new ClosePriceIndicator(series), 12, 26);
        result.setMACD(macd.getValue(countBar).doubleValue());

        StochasticOscillatorKIndicator stochasticK = new StochasticOscillatorKIndicator(series, 14);
        StochasticOscillatorDIndicator stochasticD = new StochasticOscillatorDIndicator(stochasticK);
        result.setSTOCHK(stochasticK.getValue(countBar).doubleValue());
        result.setSTOCHD(stochasticD.getValue(countBar).doubleValue());

        OnBalanceVolumeIndicator obv = new OnBalanceVolumeIndicator(series);
        result.setOBV(obv.getValue(countBar).doubleValue());

        SMAIndicator smaIndicator = new SMAIndicator(new ClosePriceIndicator(series), 14);
        result.setSMA(smaIndicator.getValue(countBar).doubleValue());

        EMAIndicator emaIndicator = new EMAIndicator(new ClosePriceIndicator(series), 14);
        result.setEMA(emaIndicator.getValue(countBar).doubleValue());

        WMAIndicator wmaIndicator = new WMAIndicator(new ClosePriceIndicator(series), 14);
        result.setWMA(wmaIndicator.getValue(countBar).doubleValue());

        ADXIndicator adxIndicator = new ADXIndicator(series, 14);
        result.setADX(adxIndicator.getValue(countBar).doubleValue());

        AroonUpIndicator up = new AroonUpIndicator(series, 14);
        AroonDownIndicator down = new AroonDownIndicator(series, 14);
        result.setAROONUP(up.getValue(countBar).doubleValue());
        result.setAROONDOWN(down.getValue(countBar).doubleValue());

        VolumeIndicator relativeVolume = new VolumeIndicator(series, 10);
        result.setRELATIVEVOLUME(relativeVolume.getValue(countBar).doubleValue());

        return result;
    }

    public static void main(String[] args) throws JsonProcessingException {
        TrainData data = new TrainData("BTCUSDT", "15m");
        System.out.println(data.getTrainData()[0]);
        System.out.println(data.getTrainResult());
        System.out.println(data.getTrainResult().get(0));
        System.out.println(data.getTrainData().length);
    }
}
