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

public class CSVRegressionClassificationFundingDataSet {
    private static final Path pathToBTCFunding =
            new File(Objects.requireNonNull(CSVCoinDataSet.class.getClassLoader().getResource("static/funding_set_single.csv")).getFile()).toPath();
    private final Path path;

    private LinkedList<TrainSetElement> data;
    private boolean isInitialized = false;
    public CSVRegressionClassificationFundingDataSet(Coin coin) {
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
            for (int i = 0; i<lines.size()-1; i++) {
                String current = lines.get(i);
                String next = lines.get(i+1);
                String[] tokensCurrent = current.split(",");
                String[] tokensNext = next.split(",");

                double value = Double.parseDouble(tokensCurrent[1])*100;

                double currentChange6price = Double.parseDouble(tokensCurrent[2]);
                double currentChange6volume = Double.parseDouble(tokensCurrent[3]);

                double currentChange4price = Double.parseDouble(tokensCurrent[4]);
                double currentChange4volume = Double.parseDouble(tokensCurrent[5]);

                double currentChange2price = Double.parseDouble(tokensCurrent[6]);
                double currentChange2volume = Double.parseDouble(tokensCurrent[7]);

                double currentChangeLastPrice = Double.parseDouble(tokensCurrent[8]);
                double currentChangeLastVolume = Double.parseDouble(tokensCurrent[9]);

                double[] data = {value, currentChange6price, currentChange6volume,
                        currentChange4price, currentChange4volume,currentChange2price,
                        currentChange2volume, currentChangeLastPrice, currentChangeLastVolume};


                double nextChange6price = Double.parseDouble(tokensNext[2]);
                double nextChange6volume = Double.parseDouble(tokensNext[3]);

                double nextChange4price = Double.parseDouble(tokensNext[4]);
                double nextChange4volume = Double.parseDouble(tokensNext[5]);

                double nextChange2price = Double.parseDouble(tokensNext[6]);
                double nextChange2volume = Double.parseDouble(tokensNext[7]);

                double nextChangeLastPrice = Double.parseDouble(tokensNext[8]);
                double nextChangeLastVolume = Double.parseDouble(tokensNext[9]);


                double medianNextChangePrice = (nextChange6price + nextChange4price + nextChange2price + nextChangeLastPrice)/4;
                double medianNextChangeVolume = (nextChange6volume + nextChange4volume + nextChange2volume + nextChangeLastVolume)/4;

                FundingClassification classification = FundingClassification.getClassification(medianNextChangePrice, medianNextChangeVolume);

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
        CSVRegressionClassificationFundingDataSet dataSet = new CSVRegressionClassificationFundingDataSet(Coin.BTCUSDT);
        dataSet.load();
        System.out.println(dataSet.getData().size());
    }
}
