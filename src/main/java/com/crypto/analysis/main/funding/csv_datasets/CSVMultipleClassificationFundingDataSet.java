package com.crypto.analysis.main.funding.csv_datasets;

import com.crypto.analysis.main.enumerations.Coin;
import com.crypto.analysis.main.funding.FundingClassification;
import com.crypto.analysis.main.ndata.CSVCoinDataSet;
import com.crypto.analysis.main.vo.TrainSetElement;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class CSVMultipleClassificationFundingDataSet {
    private static final Path pathToBTCFunding =
            new File(CSVCoinDataSet.class.getClassLoader().getResource("static/funding_set_multiple.csv").getFile()).toPath();

    private final Path path;
    private final int capacity;

    private LinkedList<TrainSetElement> data;
    private boolean isInitialized = false;

    public CSVMultipleClassificationFundingDataSet(Coin coin, int capacity) {
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
                double[][] values = new double[capacity][];
                int index = 0;
                for (int j = i; j < i + capacity; j++) {
                    String line = lines.get(j);
                    String[] tokens = line.split(",");

                    double value = Double.parseDouble(tokens[1])*100;
                    double price = Double.parseDouble(tokens[2]);
                    double volume = Double.parseDouble(tokens[3]);

                    double[] data = {value, price, volume};
                    values[index++] = data;
                }
                double firstPrice = values[0][1];
                double firstVolume = values[0][2];

                double lastPrice = values[capacity-1][1];
                double lastVolume = values[capacity-1][2];

                double priceChange = lastPrice - firstPrice;
                double volumeChange = lastVolume - firstVolume;

                FundingClassification classification = FundingClassification.getClassification(priceChange, volumeChange);
                double[][] result = new double[FundingClassification.values().length][values[0].length];

                assert classification != null;
                result[classification.ordinal()][0] = 1;

                TrainSetElement element = new TrainSetElement(values, result);
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
        CSVMultipleClassificationFundingDataSet dataSet = new CSVMultipleClassificationFundingDataSet(Coin.BTCUSDT, 10);
        dataSet.load();
        System.out.println(dataSet.data);
        System.out.println(dataSet.data.size());
    }
}
