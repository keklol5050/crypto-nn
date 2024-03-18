package com.crypto.analysis.main.data.train;

import com.crypto.analysis.main.data_utils.BinanceDataMultipleInstance;
import com.crypto.analysis.main.data_utils.enumerations.Coin;
import com.crypto.analysis.main.data_utils.enumerations.DataLength;
import com.crypto.analysis.main.data_utils.enumerations.TimeFrame;
import com.crypto.analysis.main.vo.DataObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedList;

@Getter
public class TrainDataBinance {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Coin coin;
    private final TimeFrame interval;

    private final LinkedList<DataObject[]> data = new LinkedList<>();

    private final int countInput;
    private final int countOutput;

    private final int capacity = 490;

    public TrainDataBinance(Coin coin, TimeFrame interval, DataLength  dl) {
        this.coin = coin;
        this.interval = interval;
        this.countInput = dl.getCountInput();
        this.countOutput = dl.getCountOutput();
        init();
    }


    private void init() {
        try {
            DataObject[] objects = BinanceDataMultipleInstance.getLatestInstances(coin, interval, capacity);

            int count = capacity-countOutput;

            for (int i = countInput; i < count; i++) {
                DataObject[] values = new DataObject[countInput+countOutput];
                int index = 0;

                for (int j = i-countInput; j < i+countOutput; j++) {
                    values[index++] = objects[j];
                }
                data.add(values);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        TrainDataBinance trainDataBinance = new TrainDataBinance(Coin.BTCUSDT, TimeFrame.FIFTEEN_MINUTES, DataLength.S50_3);
        for (DataObject[] objArr : trainDataBinance.getData()) {
            System.out.println(Arrays.toString(objArr));
        }

    }

}
