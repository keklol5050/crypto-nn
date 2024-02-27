package com.crypto.analysis.main.neural;

import com.crypto.analysis.main.enumerations.Periods;
import com.crypto.analysis.main.vo.DataObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Getter
public class TrainDataSet {
    private final String symbol;
    private final LinkedList<DataObject> trainData= new LinkedList<>();
    private final LinkedList<Double> trainResult = new LinkedList<>();

    private final LinkedList<double[]> finalTrainSet = new LinkedList<>();
    private final LinkedList<Double> finalTrainResult = new LinkedList<>();
    public TrainDataSet(String symbol) {
        this.symbol = symbol;
    }
    public void prepareTrainSet() {
        try {
            if (!trainResult.isEmpty() || !trainData.isEmpty()) throw new IllegalArgumentException();

            TrainData train = new TrainData(symbol, Periods.FIVE_MINUTES);
            train.updateInterval(Periods.FIFTEEN_MINUTES);
            train.updateInterval(Periods.THIRTY_MINUTES);
            train.updateInterval(Periods.ONE_HOUR);
            train.updateInterval(Periods.TWO_HOURS);
            train.updateInterval(Periods.FOUR_HOURS);
            train.updateInterval(Periods.SIX_HOURS);
            train.updateInterval(Periods.TWELVE_HOURS);
            train.updateInterval(Periods.ONE_DAY);

            trainData.addAll(train.getTrainData());
            trainResult.addAll(train.getTrainResult());

            makeTrainSet();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void makeTrainSet() {
        int count = trainData.size()/10;
        for (int i = 0; i < count; i++) {
            int index = 0;
            double[] inputDataArray = new double[210];
            for (int j = 0; j < 10; j++) {
                DataObject obj = trainData.removeLast();
                double[] params = obj.getParamArray();
                System.arraycopy(params, 0, inputDataArray, inputDataArray.length-params.length-index, params.length);
                index += params.length;
            }
            finalTrainSet.add(0,inputDataArray);
        }
        for (int i = 9; i < trainResult.size(); i+=10) {
            finalTrainResult.add(trainResult.get(i));
        }
    }

    public static void main(String[] args) {
        TrainDataSet t = new TrainDataSet("BTCUSDT");
        t.prepareTrainSet();
        List<double[]> data = t.getFinalTrainSet();
        List<Double> result = t.getFinalTrainResult();
        System.out.println(t.getTrainResult());
        for (int i = 0; i < data.size(); i++) {
            System.out.println(Arrays.toString(data.get(i)));
            System.out.println(result.get(i));
        }
        System.out.println(data.size());
        System.out.println(t.getFinalTrainSet().size());
        System.out.println(t.getFinalTrainSet().size());
    }
}
