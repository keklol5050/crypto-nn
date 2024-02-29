package com.crypto.analysis.main.neural.train;

import com.crypto.analysis.main.enumerations.Periods;
import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedList;

@Getter
public class TrainDataSet {
    private final String symbol;
    private final LinkedList<double[][]> trainData= new LinkedList<>();
    private final LinkedList<double[]> trainResult = new LinkedList<>();

    private final LinkedList<double[][]> testData = new LinkedList<>();
    private final LinkedList<double[]> testResult = new LinkedList<>();


    public TrainDataSet(String symbol) {
        this.symbol = symbol;
    }
    public void prepareTrainSet() {
            if (!trainResult.isEmpty() || !trainData.isEmpty()) throw new IllegalArgumentException();

            TrainData train = new TrainData(symbol, Periods.ONE_MINUTE);
            train.updateInterval(Periods.THREE_MINUTES);
            train.updateInterval(Periods.FIVE_MINUTES);
            train.updateInterval(Periods.FIFTEEN_MINUTES);
            train.updateInterval(Periods.THIRTY_MINUTES);
            train.updateInterval(Periods.ONE_HOUR);
            train.updateInterval(Periods.TWO_HOURS);
            train.updateInterval(Periods.FOUR_HOURS);
            train.updateInterval(Periods.SIX_HOURS);
            train.updateInterval(Periods.EIGHT_HOURS);
            train.updateInterval(Periods.TWELVE_HOURS);
            train.updateInterval(Periods.ONE_DAY);

            trainData.addAll(train.getTrainData());
            trainResult.addAll(train.getTrainResult());

            testData.addAll(train.getTestData());
            testResult.addAll(train.getTestResult());
    }


    public static void main(String[] args) {
        TrainDataSet t = new TrainDataSet("BTCUSDT");
        t.prepareTrainSet();
        LinkedList<double[][]> data = t.getTrainData();
        LinkedList<double[]> result = t.getTrainResult();
        for (int i = 0; i < data.size(); i++) {
            System.out.println(Arrays.deepToString(data.get(i)));
            System.out.println(Arrays.toString(result.get(i)));
        }
        System.out.println(data.size());
        System.out.println(t.getTrainData().size());
        System.out.println(t.getTrainResult().size());

        LinkedList<double[][]> test = t.getTestData();
        LinkedList<double[]> testResult = t.getTestResult();
        for (int i = 0; i < test.size(); i++) {
            System.out.println(Arrays.deepToString(test.get(i)));
            System.out.println(Arrays.toString(testResult.get(i)));
        }
    }
}
