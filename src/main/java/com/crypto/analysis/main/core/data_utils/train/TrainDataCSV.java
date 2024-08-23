package com.crypto.analysis.main.core.data_utils.train;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.ndata.CSVCoinDataSet;
import com.crypto.analysis.main.core.vo.DataObject;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

@Getter
public class TrainDataCSV {
    private final Coin coin;
    private final CSVCoinDataSet set;
    private final ArrayList<DataObject[]> data = new ArrayList<>();

    private final TimeFrame interval;
    private final int countInput;
    private final int countOutput;

    private final int delimiter;

    private static final Logger logger = LoggerFactory.getLogger(TrainDataCSV.class);
    public TrainDataCSV(Coin coin, TimeFrame interval, DataLength dl, CSVCoinDataSet set) {
        this.coin = coin;
        this.interval = interval;
        this.set = set;
        this.countInput = dl.getCountInput();
        this.countOutput = dl.getCountOutput();
        this.delimiter = 1;

        logger.info(String.format("Creating transformer for CSV data with coin %s, time frame %s, count input/output %d, %d",
                coin, interval, countInput, countOutput));
        init();
    }

    private void init() {
        try {
            if (!set.isInitialized()) throw new UnsupportedOperationException("CSV Data set is not initialized");
            if (set.getTf() != interval)
                throw new IllegalArgumentException("Data set timeframe is not equals to current timeframe");

            logger.info("Initializing CSV data transformer");

            ArrayList<DataObject> objects = set.getData();

            int count = objects.size() - countOutput;

            for (int i = countInput; i < count; i += delimiter) {
                DataObject[] values = new DataObject[countInput + countOutput];
                int index = 0;

                for (int j = i - countInput; j < i + countOutput; j++) {
                    values[index++] = objects.get(j);
                }
                data.add(values);
            }

            logger.info("Transforming finished");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
