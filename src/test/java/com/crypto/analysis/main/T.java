package com.crypto.analysis.main;

import com.crypto.analysis.main.core.classification.TrainDataSet;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.ndata.CSVCoinDataSet;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.nd4j.linalg.dataset.DataSet;

import java.util.Arrays;

public class T {
    public static void main(String[] args) {
        CSVCoinDataSet cs = new CSVCoinDataSet(Coin.BTCUSDT, TimeFrame.ONE_HOUR);
        cs.load();

        int numEpochs = 300;
        TrainDataSet trainDataSet = TrainDataSet.prepareTrainSet(Coin.BTCUSDT, cs);

        ListDataSetIterator<DataSet> trainIterator = trainDataSet.getTrainIterator();
        ListDataSetIterator<DataSet> testIterator = trainDataSet.getTestIterator();

        double[] matrixResult = new double[5];
        while (trainIterator.hasNext()) {
            DataSet set = trainIterator.next(1);
            double[][] data = set.getLabels().slice(0).toDoubleMatrix();
            for (int i = 0; i < data.length; i++) {
                matrixResult[i] += data[i][data[i].length-1];
            }
        }
        System.out.println(Arrays.toString(matrixResult));
    }
}
