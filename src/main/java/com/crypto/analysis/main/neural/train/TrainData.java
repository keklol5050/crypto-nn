package com.crypto.analysis.main.neural.train;

import com.crypto.analysis.main.data_utils.BinanceDataUtil;
import com.crypto.analysis.main.data_utils.IndicatorsDataUtil;
import com.crypto.analysis.main.enumerations.Periods;
import com.crypto.analysis.main.vo.CandleObject;
import com.crypto.analysis.main.vo.DataObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedList;

@Getter
public class TrainData {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final String symbol;
    private Periods interval;
    private LinkedList<CandleObject> candles;

    private final LinkedList<double[][]> trainData = new LinkedList<>();
    private final LinkedList<double[]> trainResult = new LinkedList<>();

    private final LinkedList<double[][]> testData = new LinkedList<>();
    private final LinkedList<double[]> testResult = new LinkedList<>();

    public TrainData(String symbol, Periods interval) {
        this.symbol = symbol;
        this.interval = interval;
        init();
    }

    private void init() {
        try {
            int capacity = 1361;
            int countResultParams = 4;
            int countValues = 20;

            candles = BinanceDataUtil.getCandles(symbol, interval, capacity);
            if (candles.size() < capacity) {
                while ((candles.size() - 1) % countValues != 0 || (((candles.size() - 1) / 4) % countValues != 0)) {
                    candles.removeFirst();
                }
            }

            int countMax = (candles.size() - 1) / countValues;
            int countMain = countMax - countMax / 4;
            int countMainForResult = (candles.size() - 1) - (candles.size() - 1) / 4;

            LinkedList<double[][]> localTrainData = new LinkedList<>();
            LinkedList<double[]> localTrainResult = new LinkedList<>();

            LinkedList<double[][]> localTestData = new LinkedList<>();
            LinkedList<double[]> localTestResult = new LinkedList<>();

            for (int i = countValues; i < candles.size(); i+=countValues) {
                double[] resultArr = new double[countResultParams];
                resultArr[0] = candles.get(i).getOpen()/10000;
                resultArr[1] = candles.get(i).getClose()/10000;
                resultArr[2] = candles.get(i).getLow()/10000;
                resultArr[3] = candles.get(i).getHigh()/10000;
                if (i < countMainForResult + 1) localTrainResult.add(resultArr);
                else localTestResult.add(resultArr);
            }
            candles.removeLast();

            //   IndicatorsDataUtil util = new IndicatorsDataUtil(symbol, interval);

            for (int i = 0; i < countMax; i++) {
                double[][] values = new double[countValues][];
                for (int j = 0; j < countValues; j++) {
                    DataObject obj = new DataObject(symbol, interval);
                    //     obj.setCurrentIndicators(util.getIndicators(candles.size() - 1));
                    CandleObject candle = candles.removeFirst();
                    obj.setCandle(candle);
                    values[j] = obj.getParamArray();
                }
                if (i < countMain) {
                    localTrainData.add(values);
                } else localTestData.add(values);
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
        TrainData t = new TrainData("BTCUSDT", Periods.THREE_DAYS);
        LinkedList<double[][]> data = t.getTrainData();
        LinkedList<double[]> result = t.getTrainResult();
        for (int i = 0; i < data.size(); i++) {
            System.out.println(Arrays.deepToString(data.get(i)));
            System.out.println(Arrays.toString(result.get(i)));
        }
        System.out.println(data.size());
        System.out.println(t.getTrainData().size());
        System.out.println(t.getTrainResult().size());

        LinkedList<double[][]> test = t.getTestData();
        LinkedList<double[]> testResult = t.getTestResult();
        for (int i = 0; i < test.size(); i++) {
            System.out.println(Arrays.deepToString(test.get(i)));
            System.out.println(Arrays.toString(testResult.get(i)));
        }
    }
}
