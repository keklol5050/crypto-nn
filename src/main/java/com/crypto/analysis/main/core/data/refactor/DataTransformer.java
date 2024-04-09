package com.crypto.analysis.main.core.data.refactor;

import com.crypto.analysis.main.core.data_utils.normalizers.robust.RobustScaler;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.binance.BinanceDataMultipleInstance;
import com.crypto.analysis.main.core.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.core.vo.DataObject;
import com.crypto.analysis.main.core.vo.TrainSetElement;
import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedList;

import static com.crypto.analysis.main.core.data_utils.select.StaticData.NUMBER_OF_DIFFERENTIATIONS;

public class DataTransformer {
    private final int countInput;
    private final int countOutput;

    private LinkedList<double[][]> data;

    @Getter
    private LinkedList<TrainSetElement> trainData;
    @Getter
    private RobustScaler normalizer;


    public DataTransformer(LinkedList<DataObject[]> data, DataLength dl) {
        this.countInput = dl.getCountInput()-NUMBER_OF_DIFFERENTIATIONS;
        this.countOutput = dl.getCountOutput();

        revert(data);
    }

    public static void main(String[] args) {
        DataLength dl = DataLength.S30_3;
        DataObject[] objs = BinanceDataMultipleInstance.getLatestInstances(Coin.BTCUSDT, TimeFrame.ONE_HOUR, dl.getCountInput()+dl.getCountOutput(), new FundamentalDataUtil());
        LinkedList<DataObject[]> list = new LinkedList<DataObject[]>();
        list.add(objs);

        DataTransformer transformer = new DataTransformer(list, dl);
        for (DataObject o : objs) {
            System.out.println(Arrays.toString(o.getParamArray()));
        }
        transformer.transform();
        System.out.println();
        LinkedList<TrainSetElement> data = transformer.getTrainData();
        data.forEach(System.out::println);
        System.out.println();
        double[][] tData = data.getFirst().getData();
        double[][] tResult = data.getFirst().getResult();
        for (double[] t : tData) {
            System.out.println(Arrays.toString(t));
        }
        System.out.println(Arrays.deepToString(tResult));
        System.out.println();
        transformer.normalizer.revertFeatures(tData);
        transformer.normalizer.revertLabels(tData, tResult);
        for (double[] t : tData) {
            System.out.println(Arrays.toString(t));
        }
        System.out.println(Arrays.deepToString(tResult));

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
}
