package com.crypto.analysis.main.funding.csv_datasets;

import com.crypto.analysis.main.data_utils.enumerations.Coin;
import com.crypto.analysis.main.funding.FundingClassification;
import com.crypto.analysis.main.ndata.CSVCoinDataSet;
import com.crypto.analysis.main.vo.TrainSetElement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class CSVSingleClassificationFundingDataSet {
    private static final Path pathToBTCFunding =
            new File(Objects.requireNonNull(CSVCoinDataSet.class.getClassLoader().getResource("static/funding_set_single.csv")).getFile()).toPath();
    private final Path path;

    private LinkedList<TrainSetElement> data;
    private boolean isInitialized = false;
    public CSVSingleClassificationFundingDataSet(Coin coin) {
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
            for (String line : lines) {
                String[] tokens = line.split(",");

                double value = Double.parseDouble(tokens[1])*100;

                double change6price = Double.parseDouble(tokens[2]);
                double change6volume = Double.parseDouble(tokens[3]);

                double change4price = Double.parseDouble(tokens[4]);
                double change4volume = Double.parseDouble(tokens[5]);

                double change2price = Double.parseDouble(tokens[6]);
                double change2volume = Double.parseDouble(tokens[7]);

                double changeLastPrice = Double.parseDouble(tokens[8]);
                double changeLastVolume = Double.parseDouble(tokens[9]);

                double medianChangePrice = (change6price + change4price + change2price + changeLastPrice)/4;
                double medianChangeVolume = (change6volume + change4volume + change2volume + changeLastVolume)/4;

                FundingClassification classification = FundingClassification.getClassification(medianChangePrice, medianChangeVolume);

                double[] data = {value, change6price, change6volume, change4price, change4volume,change2price,change2volume, changeLastPrice, changeLastVolume};

                assert classification != null;

                double[] result = new double[FundingClassification.values().length];

                result[classification.ordinal()] = 1;

                TrainSetElement element = new TrainSetElement(data, result);
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
        CSVSingleClassificationFundingDataSet dataSet = new CSVSingleClassificationFundingDataSet(Coin.BTCUSDT);
        dataSet.load();
        System.out.println(dataSet.data);
        System.out.println(dataSet.data.size());
    }
}
