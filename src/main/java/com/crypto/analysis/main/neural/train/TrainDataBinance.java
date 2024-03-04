package com.crypto.analysis.main.neural.train;

import com.crypto.analysis.main.data_utils.BinanceDataUtil;
import com.crypto.analysis.main.data_utils.IndicatorsDataUtil;
import com.crypto.analysis.main.enumerations.TimeFrame;
import com.crypto.analysis.main.neural.DataTransformer;
import com.crypto.analysis.main.vo.CandleObject;
import com.crypto.analysis.main.vo.DataObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedList;

@Getter
public class TrainDataBinance {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final String symbol;
    private final LinkedList<double[][]> trainData = new LinkedList<>();
    private final LinkedList<double[]> trainResult = new LinkedList<>();
    private final LinkedList<double[][]> testData = new LinkedList<>();
    private final LinkedList<double[]> testResult = new LinkedList<>();
    private TimeFrame interval;
    private LinkedList<CandleObject> candles;

    public TrainDataBinance(String symbol, TimeFrame interval) {
        this.symbol = symbol;
        this.interval = interval;
        init();
    }

    public static void main(String[] args) {
        TrainDataBinance t = new TrainDataBinance("BTCUSDT", TimeFrame.ONE_DAY);
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
        System.out.println(t.getTrainData().size());
        System.out.println(t.getTrainResult().size());

        System.out.println(t.getTestData().size());
        System.out.println(t.getTestResult().size());

    }

    private void init() {
        try {
            int capacity = 601;

            candles = BinanceDataUtil.getCandles(symbol, interval, capacity);

            IndicatorsDataUtil util = new IndicatorsDataUtil(symbol, interval);

            int countValues = 30;
            int count = candles.size() - countValues;
            int countMax = (candles.size() - 1) - ((candles.size() - 1) / 4);

            for (int i = 0; i < count; i++) {

                DataObject[] values = new DataObject[countValues+1];
                int index = 0;

                for (int j = i; j <= i + countValues; j++) {
                    DataObject obj = new DataObject(symbol, interval);
                    obj.setCurrentIndicators(util.getIndicators(candles.size() - 1 - j));
                    CandleObject candle = candles.get(j);
                    obj.setCandle(candle);
                    values[index++] = obj;
                }

                DataTransformer transformer = new DataTransformer(values);
                double[][] transformedValues = transformer.transformInput();
                double[] transformedResult = transformer.transformOutput();
                double[] finalResult = new double[3];
                finalResult[0] = transformedResult[17];
                finalResult[1] = transformedResult[18];
                finalResult[2] = transformedResult[19];
                if (i < countMax) {
                    trainData.add(transformedValues);
                    trainResult.add(finalResult);
                } else {
                    testData.add(transformedValues);
                    testResult.add(finalResult);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateInterval(TimeFrame interval) {
        this.interval = interval;
        init();
    }
}
