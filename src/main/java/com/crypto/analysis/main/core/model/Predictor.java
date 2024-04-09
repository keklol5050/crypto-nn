package com.crypto.analysis.main.core.model;

import com.crypto.analysis.main.core.data.refactor.Transposer;
import com.crypto.analysis.main.core.data.train.TrainDataSet;
import com.crypto.analysis.main.core.data_utils.normalizers.robust.RobustScaler;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.ndata.CSVCoinDataSet;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.jfree.data.xy.XYSeries;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.Nadam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Predictor {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        DataLength length = DataLength.L60_6;
        TimeFrame tf = TimeFrame.ONE_HOUR;
        CSVCoinDataSet setD = new CSVCoinDataSet(Coin.BTCUSDT, tf);
        setD.load();
        TrainDataSet trainSet = TrainDataSet.prepareTrainSet(Coin.BTCUSDT, length, setD);

        LinkedList<double[][]> inputList = trainSet.getTrainData();
        LinkedList<double[][]> outputList = trainSet.getTrainResult();

        int numInputs = inputList.get(0).length;
        int numOutputs = outputList.get(0).length;
        int sequenceLength = inputList.get(0)[0].length;
        int numEpochs = 100000;

        INDArray labelsMask = Nd4j.zeros(1, sequenceLength);
        for (int i = 0; i < length.getCountOutput(); i++) {
            labelsMask.putScalar(new int[]{0, i}, 1.0);
        }

        List<DataSet> sets = new ArrayList<>();
        for (int i = 0; i < inputList.size(); i++) {
            double[][] inputData = inputList.get(i);
            double[][] outputData = outputList.get(i);

            INDArray input = Nd4j.createFromArray(new double[][][]{inputData});
            INDArray labels = Nd4j.createFromArray(new double[][][]{outputData});

            DataSet set = new DataSet(input, labels, null, labelsMask);
            sets.add(set);
        }

        DataSetIterator iterator = new ListDataSetIterator<>(sets, 512);
        RobustScaler normalizer = trainSet.getNormalizer();
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                .weightInit(WeightInit.LECUN_NORMAL)
                .activation(Activation.TANH)
                .l2(1e-3)
                .updater(new Nadam())
                .list()
                .layer(0, new LSTM.Builder().nIn(numInputs).nOut(1600)
                        .dropOut(0.85).build())
                .layer(1, new LSTM.Builder().nIn(1600).nOut(832).build())
                .layer(2, new LSTM.Builder().nIn(832).nOut(832).build())
                .layer(3, new LSTM.Builder().nIn(832).nOut(512).build())
                .layer(4, new LSTM.Builder().nIn(512).nOut(256).build())
                .layer(5, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(256).nOut(numOutputs)
                        .build())
                .backpropType(BackpropType.Standard)
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(config);
        model.init();

        model.setListeners(new ScoreIterationListener(10));

        System.out.println(model.summary());

        System.out.println("Count input params: " + numInputs);
        System.out.println("Count output params: " + numOutputs);
        System.out.println("Count input objects: " + sequenceLength);
        System.out.println();
        System.gc();
        for (int i = 0; i < numEpochs; i++) {
            if (i % 20 == 0 && i > 0) {
                ModelLoader.saveModel(model, "D:\\model6.zip");
                System.out.println("Saved at epoch: " + model.getEpochCount());
            }
            model.fit(iterator);
            System.gc();
        }

        ModelLoader.saveModel(model, "D:\\model6.zip");


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

