package com.crypto.analysis.main.funding.csv_datasets;

import com.crypto.analysis.main.data.Transposer;
import com.crypto.analysis.main.data_utils.enumerations.Coin;
import com.crypto.analysis.main.ndata.CSVCoinDataSet;
import com.crypto.analysis.main.vo.TrainSetElement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class CSVRegressionFundingDataSet {
    private static final Path pathToBTCFunding =
            new File(CSVCoinDataSet.class.getClassLoader().getResource("static/funding_set_multiple.csv").getFile()).toPath();

    private final Path path;
    private final int capacity;

    private LinkedList<TrainSetElement> data;
    private boolean isInitialized = false;

    public CSVRegressionFundingDataSet(Coin coin, int capacity) {
        this.capacity = capacity;
        path = switch (coin) {
            case BTCUSDT -> pathToBTCFunding;
            default -> throw new IllegalArgumentException();
        };
    }

    public void load() {
        data = new LinkedList<>();
        try {
            List<String> lines = Files.readAllLines(path);
            lines.remove(0);
            for (int i = 0; i < lines.size() - capacity; i++) {
                double[][] values = new double[capacity + 1][];
                int index = 0;
                for (int j = i; j < i + capacity + 1; j++) {
                    String line = lines.get(j);
                    String[] tokens = line.split(",");

                    double value = Double.parseDouble(tokens[1])*100;
                    double price = Double.parseDouble(tokens[2]);
                    double volume = Double.parseDouble(tokens[3]);

                    double[] data = {value, price, volume};
                    values[index++] = data;
                }

                double[][] finalValues = new double[capacity][];
                System.arraycopy(values, 0, finalValues, 0, capacity);
                finalValues = Transposer.transpose(finalValues);
                double nResult = values[capacity][0];
                double[][] result = Transposer.transposeSingle(new double[]{nResult}, finalValues[0].length);
                TrainSetElement element = new TrainSetElement(finalValues, result);

                this.data.add(element);
            }
            isInitialized = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public LinkedList<TrainSetElement> getData() {
        if (!isInitialized) throw new IllegalStateException("Data set is not initialized!");
        return data;
    }

    public static void main(String[] args) {
        CSVRegressionFundingDataSet dataSet = new CSVRegressionFundingDataSet(Coin.BTCUSDT, 10);
        dataSet.load();
        System.out.println(dataSet.getData().size());
    }
}
