package com.crypto.analysis.main.data.train;

import com.crypto.analysis.main.data_utils.BinanceDataUtil;
import com.crypto.analysis.main.data_utils.IndicatorsDataUtil;
import com.crypto.analysis.main.enumerations.DataLength;
import com.crypto.analysis.main.enumerations.TimeFrame;
import com.crypto.analysis.main.vo.CandleObject;
import com.crypto.analysis.main.vo.DataObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

@Getter
public class TrainDataBinance {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String symbol;
    private final TimeFrame interval;

    private final LinkedList<DataObject[]> data = new LinkedList<>();

    private final int countInput;
    private final int countOutput;

    private LinkedList<CandleObject> candles;
    private final int capacity = 600;

    public TrainDataBinance(String symbol, TimeFrame interval, DataLength  dl) {
        this.symbol = symbol;
        this.interval = interval;
        this.countInput = dl.getCountInput();
        this.countOutput = dl.getCountOutput();
        init();
    }


    private void init() {
        try {
            candles = BinanceDataUtil.getCandles(symbol, interval, capacity);

            IndicatorsDataUtil util = new IndicatorsDataUtil(symbol, interval);

            int count = candles.size()-DataLength.MAX_OUTPUT_LENGTH;

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
                data.add(values);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
