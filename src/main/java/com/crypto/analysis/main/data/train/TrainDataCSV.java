package com.crypto.analysis.main.data.train;

import com.crypto.analysis.main.data_utils.IndicatorsDataUtil;
import com.crypto.analysis.main.enumerations.DataLength;
import com.crypto.analysis.main.enumerations.TimeFrame;
import com.crypto.analysis.main.ndata.CSVHourAndDayTF;
import com.crypto.analysis.main.data.DataTransformer;
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
    private final LinkedList<double[][]> trainData = new LinkedList<>();
    private final LinkedList<double[][]> trainResult = new LinkedList<>();
    private final TimeFrame interval;
    private final int countInput;
    private final int countOutput;

    public TrainDataCSV(String symbol, TimeFrame interval, DataLength dl) {
        this.symbol = symbol;
        this.interval = interval;
        this.countInput = dl.getCountInput();
        this.countOutput = dl.getCountOutput();
        init();
    }


    public static void main(String[] args) {
        TrainDataCSV t = new TrainDataCSV("BTCUSDT", TimeFrame.ONE_DAY, DataLength.D30_5);
        LinkedList<double[][]> data = t.getTrainData();
        LinkedList<double[][]> result = t.getTrainResult();
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
        System.out.println(data.size());
        System.out.println(result.size());
    }

    private void init() {
        try {
            LinkedList<CandleObject> candles;

            if (interval == TimeFrame.ONE_HOUR) {
                candles = CSVHourAndDayTF.getHourCandles();
            } else if (interval == TimeFrame.ONE_DAY) {
                candles = CSVHourAndDayTF.getDayCandles();
            } else throw new IllegalArgumentException();

            List<CandleObject> unModCandles = new LinkedList<>(candles);
            IndicatorsDataUtil util = new IndicatorsDataUtil(Collections.unmodifiableList(unModCandles));

            int count = candles.size()-DataLength.MAX_OUTPUT_LENGTH;

            for (int i = DataLength.MAX_INPUT_LENGTH; i < count; i++) {

                DataObject[] values = new DataObject[countInput+countOutput];
                int index = 0;

                for (int j = i-countInput; j < i+countOutput; j++) {
                    DataObject obj = new DataObject(symbol, interval);
                    obj.setCurrentIndicators(util.getIndicators(j));
                    CandleObject candle = candles.get(j);
                    obj.setCandle(candle);
                    values[index++] = obj;
                }

                DataTransformer transformer = new DataTransformer(values, countInput, countOutput);
                double[][] transformedValues = transformer.transformInput();
                double[][] transformedResult = transformer.transformOutput();

                trainData.add(transformedValues);
                trainResult.add(transformedResult);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
