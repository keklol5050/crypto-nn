package com.crypto.analysis.main.neural.train;

import com.crypto.analysis.main.data_utils.IndicatorsDataUtil;
import com.crypto.analysis.main.data_utils.ndata.CSVHourAndDayTF;
import com.crypto.analysis.main.enumerations.Periods;
import com.crypto.analysis.main.neural.DataTransformer;
import com.crypto.analysis.main.vo.CandleObject;
import com.crypto.analysis.main.vo.DataObject;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Getter
public class TrainDataCSV {
    private final String symbol;
    private Periods interval;

    private final LinkedList<double[][]> trainData = new LinkedList<>();
    private final LinkedList<double[]> trainResult = new LinkedList<>();

    public TrainDataCSV(String symbol, Periods interval) {
        this.symbol = symbol;
        this.interval = interval;
        init();
    }

    private void init() {
        try {
            LinkedList<CandleObject> candles;

            if (interval == Periods.ONE_HOUR) {
                candles = CSVHourAndDayTF.getHourCandles();
            } else if (interval == Periods.ONE_DAY) {
                candles = CSVHourAndDayTF.getDayCandles();
            } else throw new IllegalArgumentException();

            List<CandleObject> unModCandles = new LinkedList<>(candles);
            IndicatorsDataUtil util = new IndicatorsDataUtil(Collections.unmodifiableList(unModCandles));

            int countValues = 30;
            int count = candles.size() - countValues;

            for (int i = 0; i < count; i++) {
                double[][] values = new double[countValues][];
                int index = 0;

                for (int j = i; j < i + countValues; j++) {
                    DataObject obj = new DataObject(symbol, interval);
                    obj.setCurrentIndicators(util.getIndicators(j));
                    CandleObject candle = candles.get(j);
                    obj.setCandle(candle);
                    values[index++] = obj.getParamArray();
                }
                DataObject nextObj = new DataObject(symbol, interval);
                nextObj.setCurrentIndicators(util.getIndicators(i + countValues));
                nextObj.setCandle(candles.get(i + countValues));
                double[] result = nextObj.getParamArray();

                DataTransformer transformer = new DataTransformer(values, result);
                double[][] transformedValues = transformer.transformInput();
                double[] transformedResult = transformer.transformOutput();

                trainData.add(transformedValues);
                trainResult.add(transformedResult);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateInterval(Periods interval) {
        this.interval = interval;
        init();
    }

    public static void main(String[] args) {
        TrainDataCSV t = new TrainDataCSV("BTCUSDT", Periods.ONE_DAY);
        LinkedList<double[][]> data = t.getTrainData();
        LinkedList<double[]> result = t.getTrainResult();
        for (int i = 0; i < data.size(); i++) {
            System.out.println(Arrays.deepToString(data.get(i)));
            System.out.println(Arrays.toString(result.get(i)));
        }
        System.out.println(data.size());
        System.out.println(result.size());
    }
}
