package com.crypto.analysis.main.data.train;

import com.crypto.analysis.main.data_utils.select.StaticData;
import com.crypto.analysis.main.data_utils.select.coin.Coin;
import com.crypto.analysis.main.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.data_utils.utils.binance.BinanceDataMultipleInstance;
import com.crypto.analysis.main.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.vo.DataObject;
import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedList;

@Getter
public class TrainDataBinance {
    private final Coin coin;
    private final TimeFrame interval;

    private final LinkedList<DataObject[]> data = new LinkedList<>();

    private final int countInput;
    private final int countOutput;
    private final FundamentalDataUtil fdUtil;

    private final int capacity = 490;

    public TrainDataBinance(Coin coin, TimeFrame interval, DataLength  dl, FundamentalDataUtil fdUtil) {
        this.coin = coin;
        this.interval = interval;
        this.countInput = dl.getCountInput();
        this.countOutput = dl.getCountOutput();
        this.fdUtil = fdUtil;
        init();
    }


    private void init() {
        try {
            DataObject[] objects = BinanceDataMultipleInstance.getLatestInstances(coin, interval, capacity, fdUtil);

            int count = capacity-countOutput;
            int delimiter = StaticData.getDelimiterForBinance(interval);

            for (int i = countInput; i < count; i+=delimiter) {
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
        TrainDataBinance trainDataBinance = new TrainDataBinance(Coin.BTCUSDT, TimeFrame.ONE_HOUR, DataLength.S50_3, new FundamentalDataUtil());
        for (DataObject[] objArr : trainDataBinance.getData()) {
            System.out.println(Arrays.toString(objArr));
        }

    }

}
