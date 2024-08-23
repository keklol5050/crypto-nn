package com.crypto.analysis.main.core.data_utils.train;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.BinanceDataUtil;
import com.crypto.analysis.main.core.vo.DataObject;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

@Getter
public class TrainDataBinance {
    private final Coin coin;
    private final TimeFrame interval;

    private final ArrayList<DataObject[]> data = new ArrayList<>();

    private final int countInput;
    private final int countOutput;

    private final int delimiter;

    private static final Logger logger = LoggerFactory.getLogger(TrainDataBinance.class);
    public static final int binanceCapacityMax = 490;

    public TrainDataBinance(Coin coin, TimeFrame interval, DataLength dl) {
        this.coin = coin;
        this.interval = interval;
        this.countInput = dl.getCountInput();
        this.countOutput = dl.getCountOutput();
        this.delimiter = 1;

        logger.info(String.format("Creating transformer for Binance data with coin %s, time frame %s, count input/output %d, %d",
                coin, interval, countInput, countOutput));
        init();
    }

    private void init() {
        try {
            logger.info("Initializing Binance data transformer");

            DataObject[] objects = BinanceDataUtil.getLatestInstances(coin, interval, binanceCapacityMax);

            int count = binanceCapacityMax - countOutput;

            for (int i = countInput; i < count; i += delimiter) {
                DataObject[] values = new DataObject[countInput + countOutput];
                int index = 0;

                for (int j = i - countInput; j < i + countOutput; j++) {
                    values[index++] = objects[j];
                }
                data.add(values);
            }

            logger.info("Transforming finished");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
