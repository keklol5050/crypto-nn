package com.crypto.analysis.main;

import com.crypto.analysis.main.core.data_utils.normalizers.Transposer;
import com.crypto.analysis.main.core.regression.RegressionDataSet;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.mutils.DataVisualisation;
import com.crypto.analysis.main.core.data_utils.utils.mutils.ModelLoader;
import com.crypto.analysis.main.core.ndata.CSVCoinDataSet;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.jfree.data.xy.XYSeries;
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;

import java.util.*;

public class ModeLoadTest {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        DataLength length = DataLength.L100_6;
        TimeFrame tf = TimeFrame.ONE_HOUR;

        CSVCoinDataSet setD = new CSVCoinDataSet(Coin.BTCUSDT, tf);
        setD.load();

        RegressionDataSet regressionDataSet = RegressionDataSet.prepareTrainSet(Coin.BTCUSDT, length, setD);
        ListDataSetIterator<DataSet> trainIterator = regressionDataSet.getTrainIterator();
        ListDataSetIterator<DataSet> testIterator = regressionDataSet.getTestIterator();

        INDArray labelsMask = regressionDataSet.getLabelsMask();

        System.out.println(trainIterator.next(1));
        System.out.println();
        System.out.println();
        System.out.println(testIterator.next(1));

        MultiLayerNetwork model = ModelLoader.loadNetwork("D:\\model15.zip");
        model.init();

        System.out.println(model.summary());

        XYSeries predT = new XYSeries("Predicted");
        XYSeries realT = new XYSeries("Real");
        int index1 = 0;
        int index2 = 0;
        int countRight = 0;

        RegressionEvaluation eval = new RegressionEvaluation();
        System.out.println("Evaluating validation set...");
        for (int i = 0; i < 100; i++){
            DataSet set = testIterator.next(1);
            INDArray futures = set.getFeatures();
            INDArray labels = set.getLabels();

            INDArray predictedOutput = model.output(futures, false, null, labelsMask);
            eval.eval(labels, predictedOutput, labelsMask);

            double[][] features = futures.slice(0).toDoubleMatrix();

            double[][] newFeaturesArr = new double[4][];
            System.arraycopy(features, 0, newFeaturesArr, 0, 4);

            double[][] real = labels.slice(0).toDoubleMatrix();

            double[][] predMatrix = predictedOutput.slice(0).toDoubleMatrix();

            double[][] predicted = new double[regressionDataSet.getCountOutput()][length.getCountOutput()];
            for (int j = 0; j < regressionDataSet.getCountOutput(); j++) {
                System.arraycopy(predMatrix[j], 0, predicted[j], 0, length.getCountOutput());
            }

            double[][] realFin = new double[regressionDataSet.getCountOutput()][length.getCountOutput()];
            for (int j = 0; j < regressionDataSet.getCountOutput(); j++) {
                System.arraycopy(real[j], 0, realFin[j], 0, length.getCountOutput());
            }
            DataVisualisation.visualizeData("Prediction", "candle length", "price", newFeaturesArr[3], realFin[2], predicted[2]);
            for (double d : realFin[2]) {
                realT.add(index1++, d);
            }
            for (double d : predicted[2]) {
                predT.add(index2++, d);
            }

            newFeaturesArr = Transposer.transpose(newFeaturesArr);
            predicted = Transposer.transpose(predicted);
            realFin = Transposer.transpose(realFin);

            int rows = predicted.length;
            int cols = predicted[0].length;
            double totalDifference = 0.0;

            for (int k = 0; k < rows; k++) {
                for (int j = 0; j < cols; j++) {
                    double realValue = realFin[k][j];
                    double predictedValue = predicted[k][j];
                    double percentageDifference = Math.abs(predictedValue - realValue);
                    totalDifference += percentageDifference;
                }
            }
            totalDifference /= rows*cols;
            double mean = Math.abs(realFin[2][0]*0.05);
            if (Math.abs(totalDifference) < mean)
                countRight++;

            System.out.println("Input data: ");
            for (double[] arr : newFeaturesArr) {
                System.out.println(Arrays.toString(arr));
            }
            System.out.println("=============================================================");
            System.out.println("Predicted: ");
            for (double[] arr : predicted) {
                System.out.println(Arrays.toString(arr));
            }
            System.out.println("=============================================================");
            System.out.println("Real: ");
            for (double[] arr : realFin) {
                System.out.println(Arrays.toString(arr));
            }
            System.out.println();
            System.out.println("Total difference " + totalDifference + ", mean: " + mean);
            System.out.println("=============================================================");
            System.out.println();

        }
        System.out.println("Time taken: " + ((System.currentTimeMillis() - start) / 1000) / 60 + " minutes");
        System.out.println(eval.stats());
        DataVisualisation.visualize("Predictions", "candle", "price", predT, realT);
    }
}
