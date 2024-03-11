package com.crypto.analysis.main.data.train;

import com.crypto.analysis.main.data_utils.IndicatorsDataUtil;
import com.crypto.analysis.main.enumerations.DataLength;
import com.crypto.analysis.main.enumerations.TimeFrame;
import com.crypto.analysis.main.ndata.CSVHourAndDayTF;
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
    private final LinkedList<DataObject[]> data = new LinkedList<>();

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

                data.add(values);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
