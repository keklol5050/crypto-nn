package com.crypto.analysis.main.funding;

import com.crypto.analysis.main.enumerations.Coin;
import com.crypto.analysis.main.funding.csv_datasets.CSVMultipleClassificationFundingDataSet;
import com.crypto.analysis.main.funding.csv_datasets.CSVRegressionFundingDataSet;
import com.crypto.analysis.main.funding.csv_datasets.CSVSingleClassificationFundingDataSet;
import com.crypto.analysis.main.vo.TrainSetElement;

import java.util.Collections;
import java.util.LinkedList;

public class FundingTrainSetFactory {
    public static FundingTrainSet getInstance(Coin coin, int capacity, int batchSize) {
        // single classification set
        CSVSingleClassificationFundingDataSet single = new CSVSingleClassificationFundingDataSet(coin);
        single.load();
        LinkedList<TrainSetElement> elements = single.getData();
        Collections.shuffle(elements);

        LinkedList<double[]> singleTrainData =  new LinkedList<>();
        LinkedList<double[]> singeTrainResult =  new LinkedList<>();

        LinkedList<double[]> singleTestData = new LinkedList<>();
        LinkedList<double[]> singleTestResult = new LinkedList<>();

        int count = elements.size();
        int max = count-count/6;

        for (int i = 0; i < count; i++) {
            if (i < max) {
                singleTrainData.add(elements.get(i).getDataArray());
                singeTrainResult.add(elements.get(i).getResultArray());
            } else {
                singleTestData.add(elements.get(i).getDataArray());
                singleTestResult.add(elements.get(i).getResultArray());
            }
        }

        // multiple classification set
        CSVMultipleClassificationFundingDataSet multiple = new CSVMultipleClassificationFundingDataSet(coin, capacity);
        multiple.load();
        elements = multiple.getData();
        Collections.shuffle(elements);

        LinkedList<double[][]> multipleTrainData =  new LinkedList<>();
        LinkedList<double[][]> multipleTrainResult =  new LinkedList<>();

        LinkedList<double[][]> multipleTestData = new LinkedList<>();
        LinkedList<double[][]> multipleTestResult = new LinkedList<>();

        count = elements.size();
        max = count-count/6;

        for (int i = 0; i < count; i++) {
            if (i < max) {
                multipleTrainData.add(elements.get(i).getDataMatrix());
                multipleTrainResult.add(elements.get(i).getResultMatrix());
            } else {
                multipleTestData.add(elements.get(i).getDataMatrix());
                multipleTestResult.add(elements.get(i).getResultMatrix());
            }
        }

        // regression set
        CSVRegressionFundingDataSet regression = new CSVRegressionFundingDataSet(coin, capacity);
        regression.load();
        elements = regression.getData();
        Collections.shuffle(elements);

        LinkedList<double[][]> regressionTrainData =  new LinkedList<>();
        LinkedList<double[][]> regressionTrainResult = new LinkedList<>();

        LinkedList<double[][]> regressionTestData = new LinkedList<>();
        LinkedList<double[][]> regressionTestResult = new LinkedList<>();

        count = elements.size();
        max = count-count/6;

        for (int i = 0; i < count; i++) {
            if (i < max) {
                regressionTrainData.add(elements.get(i).getDataMatrix());
                regressionTrainResult.add(elements.get(i).getResultMatrix());
            } else {
                regressionTestData.add(elements.get(i).getDataMatrix());
                regressionTestResult.add(elements.get(i).getResultMatrix());
            }
        }

        return new FundingTrainSet(coin, singleTrainData, singeTrainResult,
                multipleTrainData, multipleTrainResult,
                regressionTrainData, regressionTrainResult,
                singleTestData, singleTestResult, multipleTestData,
                multipleTestResult, regressionTestData, regressionTestResult,
                batchSize, capacity);
    }

    public static void main(String[] args) {
        getInstance(Coin.BTCUSDT, 10, 1);
    }
}
