package com.crypto.analysis.main.data;

import com.crypto.analysis.main.data_utils.BinanceDataMultipleInstance;
import com.crypto.analysis.main.enumerations.DataLength;
import com.crypto.analysis.main.enumerations.TimeFrame;
import com.crypto.analysis.main.vo.DataObject;
import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedList;

@Getter
public class DataTransformer {
    private final int countInput;
    private final int countOutput;

    private LinkedList<double[][]> data;
    private LinkedList<double[][]> trainData;
    private LinkedList<double[][]> trainResult;

    private DataNormalizer normalizer;
    public DataTransformer(LinkedList<DataObject[]> data, DataLength dl) {
        this.countInput = dl.getCountInput();
        this.countOutput = dl.getCountOutput();

        revert (data);
    }

    private void revert(LinkedList<DataObject[]> data) {
        this.data = new LinkedList<>();
        for (DataObject[] datum : data) {
            double[][] doArray = new double[datum.length][];
            for (int j = 0; j < datum.length; j++) {
                doArray[j] = datum[j].getParamArray();
            }
            this.data.add(doArray);
        }
    }

    public void transform() {
        normalizer = new DataNormalizer();
        normalizer.fit(data);
        normalizer.transform(data);
        prepareData();
    }

    private void prepareData() {
        trainData = new LinkedList<>();
        trainResult = new LinkedList<>();
        for (double[][] datum : data) {
            DataRefactor transformer = new DataRefactor(datum, countInput, countOutput);
            trainData.add(transformer.transformInput());
            trainResult.add(transformer.transformOutput());
        }
    }

    public static void main(String[] args) {
        DataObject[] objs = BinanceDataMultipleInstance.getLatestInstances("BTCUSDT", TimeFrame.ONE_HOUR);
        LinkedList<DataObject[]> list = new LinkedList<DataObject[]>();
        list.add(objs);
        DataTransformer transformer = new DataTransformer(list, DataLength.S50_3);
        for (DataObject o : objs) {
            System.out.println(Arrays.toString(o.getParamArray()));
        }
        transformer.transform();
        LinkedList<double[][]> data = transformer.getTrainData();
        for (double[][] datum : data) {
           for (int i = 0; i < datum.length; i++) {
               System.out.println(Arrays.toString(datum[i]));
           }
        }
        LinkedList<double[][]> res = transformer.getTrainResult();
        for (double[][] datum : res) {
            for (int i = 0; i < datum.length; i++) {
                System.out.println(Arrays.toString(datum[i]));
            }
        }
    }
}
