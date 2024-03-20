package com.crypto.analysis.main.data.refactor;

import com.crypto.analysis.main.data_utils.utils.binance.BinanceDataMultipleInstance;
import com.crypto.analysis.main.data_utils.normalizers.BatchNormalizer;
import com.crypto.analysis.main.data_utils.select.coin.Coin;
import com.crypto.analysis.main.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.vo.DataObject;
import com.crypto.analysis.main.vo.TrainSetElement;
import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedList;

@Getter
public class DataTransformer {
    private final int countInput;
    private final int countOutput;

    private LinkedList<double[][]> data;
    private LinkedList<TrainSetElement> trainData;

    private BatchNormalizer normalizer;

    public DataTransformer(LinkedList<DataObject[]> data, DataLength dl) {
        this.countInput = dl.getCountInput();
        this.countOutput = dl.getCountOutput();

        revert(data);
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
        trainData = new LinkedList<>();
        DataRefactor refactor = new DataRefactor(data, countInput, countOutput);
        trainData.addAll(refactor.transform());
        normalizer = refactor.getNormalizer();
    }

    public static void main(String[] args) {
        DataObject[] objs = BinanceDataMultipleInstance.getLatestInstances(Coin.BTCUSDT, TimeFrame.FIFTEEN_MINUTES, 53);
        LinkedList<DataObject[]> list = new LinkedList<DataObject[]>();
        list.add(objs);
        DataTransformer transformer = new DataTransformer(list, DataLength.S50_3);
        for (DataObject o : objs) {
            System.out.println(Arrays.toString(o.getParamArray()));
        }
        transformer.transform();
        System.out.println();
        LinkedList<TrainSetElement> data = transformer.getTrainData();
        data.forEach(System.out::println);
        System.out.println();
        double[][] tData = data.getFirst().getDataMatrix();
        double[][] tResult = data.getFirst().getResultMatrix();
        for (double[] t : tData) {
            System.out.println(Arrays.toString(t));
        }
        System.out.println(Arrays.deepToString(tResult));
        System.out.println();
        transformer.normalizer.revertFeaturesVertical(tData);
        transformer.normalizer.revertLabelsVertical(tData, tResult);
        for (double[] t : tData) {
            System.out.println(Arrays.toString(t));
        }
        System.out.println(Arrays.deepToString(tResult));

    }
}
