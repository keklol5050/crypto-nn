package com.crypto.analysis.main;

import com.crypto.analysis.main.core.data.refactor.Transposer;
import com.crypto.analysis.main.core.data.train.TrainDataSet;
import com.crypto.analysis.main.core.data_utils.normalizers.RobustScaler;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.model.DataVisualisation;
import com.crypto.analysis.main.core.model.ModelLoader;
import com.crypto.analysis.main.core.ndata.CSVCoinDataSet;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.conf.layers.recurrent.Bidirectional;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.jfree.data.xy.XYSeries;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ModeLoadTest {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        DataLength length = DataLength.L60_6;
        TimeFrame tf = TimeFrame.ONE_HOUR;
        CSVCoinDataSet setD = new CSVCoinDataSet(Coin.BTCUSDT, tf);
        setD.load();
        TrainDataSet trainSet = TrainDataSet.prepareTrainSet(Coin.BTCUSDT, length, setD);

        LinkedList<double[][]> inputList = trainSet.getTrainData();
        LinkedList<double[][]> outputList = trainSet.getTrainResult();

        int numOutputs = outputList.get(0).length;
        int sequenceLength = inputList.get(0)[0].length;

        INDArray labelsMask = Nd4j.zeros(1, sequenceLength);
        for (int i = 0; i < length.getCountOutput(); i++) {
            labelsMask.putScalar(new int[]{0, i}, 1.0);
        }


        RobustScaler normalizer = trainSet.getNormalizer();

        MultiLayerNetwork model = ModelLoader.loadNetwork("D:\\model18.zip");
        model.init();

        System.out.println(model.summary());

        LinkedList<double[][]> testSet = trainSet.getTestData();
        LinkedList<double[][]> testResult = trainSet.getTestResult();

        XYSeries predT = new XYSeries("Predicted");
        XYSeries realT = new XYSeries("Real");
        int index1 = 0;
        int index2 = 0;
        int countRight = 0;
        for (int i = 0; i < testSet.size(); i++) {
            INDArray newInput = Nd4j.createFromArray(new double[][][]{testSet.get(i)});
            INDArray predictedOutput = model.output(newInput, false, null, labelsMask);

            double[][] features = testSet.get(i);
            normalizer.revertFeatures(features);

            double[][] newFeaturesArr = new double[4][];
            System.arraycopy(features, 0, newFeaturesArr, 0, 4);

            double[][] real = testResult.get(i);
            normalizer.revertLabels(testSet.get(i), real);

            double[][] predMatrix = predictedOutput.slice(0).toDoubleMatrix();
            normalizer.revertLabels(testSet.get(i), predMatrix);

            double[][] predicted = new double[numOutputs][length.getCountOutput()];
            for (int j = 0; j < numOutputs; j++) {
                System.arraycopy(predMatrix[j], 0, predicted[j], 0, length.getCountOutput());
            }

            double[][] realFin = new double[numOutputs][length.getCountOutput()];
            for (int j = 0; j < numOutputs; j++) {
                System.arraycopy(real[j], 0, realFin[j], 0, length.getCountOutput());
            }
            DataVisualisation.visualizeData("Prediction", "candle length", "price", newFeaturesArr[3], realFin[0], predicted[0]);
            for (double d : realFin[0]) {
                realT.add(index1++, d);
            }
            for (double d : predicted[0]) {
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
        System.out.println("Percentage: " + ((double) countRight / (double) testSet.size()) * 100 + '%');
        System.out.println("Time taken: " + ((System.currentTimeMillis() - start) / 1000) / 60 + " minutes");
        DataVisualisation.visualize("Predictions", "candle", "price", predT, realT);
    }
}
