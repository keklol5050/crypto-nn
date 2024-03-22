package com.crypto.analysis.main.data.train;

import com.crypto.analysis.main.data_utils.select.StaticData;
import com.crypto.analysis.main.data_utils.select.coin.Coin;
import com.crypto.analysis.main.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.ndata.CSVCoinDataSet;
import com.crypto.analysis.main.vo.DataObject;
import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedList;

@Getter
public class TrainDataCSV {
    private final Coin coin;
    private final CSVCoinDataSet set;
    private final LinkedList<DataObject[]> data = new LinkedList<>();

    private final TimeFrame interval;
    private final int countInput;
    private final int countOutput;

    public TrainDataCSV(Coin coin, TimeFrame interval, DataLength dl, CSVCoinDataSet set) {
        this.coin = coin;
        this.interval = interval;
        this.set = set;
        this.countInput = dl.getCountInput();
        this.countOutput = dl.getCountOutput();
        init();
    }

    private void init() {
        try {
            if (!set.isInitialized()) throw new UnsupportedOperationException("CSV Data set is not initialized");
            if (set.getInterval() != interval) throw new IllegalArgumentException("Data set timeframe is not equals to current timeframe");
            LinkedList<DataObject> objects = set.getData();

            int count = objects.size()-countOutput;
            int delimiter = StaticData.getDelimiterForSet(interval);

            for (int i = countInput; i < count; i+=delimiter) {
                DataObject[] values = new DataObject[countInput+countOutput];
                int index = 0;

                for (int j = i-countInput; j < i+countOutput; j++) {
                    values[index++] = objects.get(j);
                }
                data.add(values);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        CSVCoinDataSet csv = new CSVCoinDataSet(Coin.BTCUSDT, TimeFrame.FIFTEEN_MINUTES);
        csv.load();
        TrainDataCSV trainDataCSV = new TrainDataCSV(Coin.BTCUSDT, TimeFrame.FIFTEEN_MINUTES, DataLength.S50_3, csv);
        for (DataObject[] objects : trainDataCSV.getData()) {
            System.out.println(Arrays.toString(objects));
        }
    }

}
