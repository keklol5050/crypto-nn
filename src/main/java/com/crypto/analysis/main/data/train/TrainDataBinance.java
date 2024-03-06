package com.crypto.analysis.main.data.train;

import com.crypto.analysis.main.data_utils.BinanceDataUtil;
import com.crypto.analysis.main.data_utils.IndicatorsDataUtil;
import com.crypto.analysis.main.enumerations.DataLength;
import com.crypto.analysis.main.enumerations.TimeFrame;
import com.crypto.analysis.main.data.DataTransformer;
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
    private final LinkedList<double[][]> trainResult = new LinkedList<>();
    private final LinkedList<double[][]> testData = new LinkedList<>();
    private final LinkedList<double[][]> testResult = new LinkedList<>();
    private final TimeFrame interval;
    private final int countInput;
    private final int countOutput;

    private LinkedList<CandleObject> candles;
    int capacity = 600;

    public TrainDataBinance(String symbol, TimeFrame interval, DataLength  dl) {
        this.symbol = symbol;
        this.interval = interval;
        this.countInput = dl.getCountInput();
        this.countOutput = dl.getCountOutput();
        init();
    }

    public static void main(String[] args) {
        TrainDataBinance t = new TrainDataBinance("BTCUSDT", TimeFrame.ONE_HOUR, DataLength.D100_15);
        LinkedList<double[][]> data = t.getTrainData();
        LinkedList<double[][]> result = t.getTrainResult();
        for (int i = 0; i < data.size(); i++) {
            System.out.println(Arrays.deepToString(data.get(i)));
            System.out.println(Arrays.deepToString(result.get(i)));
        }
        System.out.println(data.size());
        System.out.println(t.getTrainData().size());
        System.out.println(t.getTrainResult().size());

        LinkedList<double[][]> test = t.getTestData();
        LinkedList<double[][]> testResult = t.getTestResult();
        for (int i = 0; i < test.size(); i++) {
            System.out.println(Arrays.deepToString(test.get(i)));
            System.out.println(Arrays.deepToString(testResult.get(i)));
        }
        System.out.println(t.getTrainData().size());
        System.out.println(t.getTrainResult().size());

        System.out.println(t.getTestData().size());
        System.out.println(t.getTestResult().size());
        System.out.println(test.get(1).length);
        System.out.println(testResult.get(1).length);

        double[][] datal = data.getLast();
        double[][] resul = result.getLast();
        for (int i = 0; i < datal.length; i++) {
            System.out.println(Arrays.toString(datal[i]));
        }
        System.out.println();
        System.out.println();
        for (int i = 0; i < resul.length; i++) {
            System.out.println(Arrays.toString(resul[i]));
        }
    }

    private void init() {
        try {
            candles = BinanceDataUtil.getCandles(symbol, interval, capacity);

            IndicatorsDataUtil util = new IndicatorsDataUtil(symbol, interval);

            int count = candles.size()-DataLength.MAX_OUTPUT_LENGTH;
            int countMax = count - (count / 4);

            for (int i = DataLength.MAX_INPUT_LENGTH; i < count; i++) {

                DataObject[] values = new DataObject[countInput+countOutput];
                int index = 0;

                for (int j = i-countInput; j < i+countOutput; j++) {
                    DataObject obj = new DataObject(symbol, interval);
                    obj.setCurrentIndicators(util.getIndicators(candles.size() - 1 - j));
                    CandleObject candle = candles.get(j);
                    obj.setCandle(candle);
                    values[index++] = obj;
                }

                DataTransformer transformer = new DataTransformer(values, countInput, countOutput);
                double[][] transformedValues = transformer.transformInput();
                double[][] transformedResult = transformer.transformOutput();

                if (i < countMax) {
                    trainData.add(transformedValues);
                    trainResult.add(transformedResult);
                } else {
                    testData.add(transformedValues);
                    testResult.add(transformedResult);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
