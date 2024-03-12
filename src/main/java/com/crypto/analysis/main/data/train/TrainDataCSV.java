package com.crypto.analysis.main.data.train;

import com.crypto.analysis.main.enumerations.Coin;
import com.crypto.analysis.main.enumerations.DataLength;
import com.crypto.analysis.main.enumerations.TimeFrame;
import com.crypto.analysis.main.ndata.CSVDataSet;
import com.crypto.analysis.main.vo.DataObject;
import lombok.Getter;

import java.util.LinkedList;

@Getter
public class TrainDataCSV {
    private final Coin coin;
    private final CSVDataSet set;
    private final LinkedList<DataObject[]> data = new LinkedList<>();

    private final TimeFrame interval;
    private final int countInput;
    private final int countOutput;

    public TrainDataCSV(Coin coin, TimeFrame interval, DataLength dl, CSVDataSet set) {
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
            LinkedList<DataObject> objects = set.getData();
            int count = objects.size()-DataLength.MAX_OUTPUT_LENGTH;

            for (int i = DataLength.MAX_INPUT_LENGTH; i < count; i++) {
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

}
