package com.crypto.analysis.main.model;

import com.crypto.analysis.main.data.refactor.Transposer;
import com.crypto.analysis.main.data.train.TrainDataSet;
import com.crypto.analysis.main.data_utils.normalizers.RobustNormalizer;
import com.crypto.analysis.main.data_utils.select.coin.Coin;
import com.crypto.analysis.main.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.ndata.CSVCoinDataSet;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DropoutLayer;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.buffer.DataType;
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

public class Predictor {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        DataLength length = DataLength.S50_3;
        TimeFrame tf = TimeFrame.ONE_HOUR;
        CSVCoinDataSet setD = new CSVCoinDataSet(Coin.BTCUSDT, tf);
        setD.load();
        TrainDataSet trainSet = TrainDataSet.prepareTrainSet(Coin.BTCUSDT, length, setD);

        LinkedList<double[][]> inputList = trainSet.getTrainData();
        LinkedList<double[][]> outputList = trainSet.getTrainResult();

        int numInputs = inputList.get(0).length;
        int numOutputs = outputList.get(0).length;
        int sequenceLength = inputList.get(0)[0].length;
        int numEpochs = 100;

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

        DataSetIterator iterator = new ListDataSetIterator<>(sets, 64);
        RobustNormalizer normalizer = trainSet.getNormalizer();
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                .l2(5e-5)
                .dataType(DataType.DOUBLE)
                .weightInit(WeightInit.XAVIER)
                .activation(Activation.TANH)
                .updater(new Adam(0.005))
                .list()
                .layer(0, new LSTM.Builder().nIn(numInputs).nOut(256).dropOut(0.8).build())
                .layer(1, new LSTM.Builder().nIn(256).nOut(1024).dropOut(0.5).build())
                .layer(2, new LSTM.Builder().nIn(1024).nOut(1024).dropOut(0.8).build())
                .layer(3, new LSTM.Builder().nIn(1024).nOut(256).build())
                .layer(4, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
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
            if (i % 5 == 0 && i > 0)
                ModelLoader.saveModel(model, "D:\\model2.zip");

            model.fit(iterator);
            System.gc();
        }

        ModelLoader.saveModel(model, "D:\\model2.zip");

        LinkedList<double[][]> testSet = trainSet.getTestData();
        LinkedList<double[][]> testResult = trainSet.getTestResult();

        int countRight = 0;
        for (int i = 0; i < testSet.size(); i++) {
            INDArray newInput = Nd4j.createFromArray(new double[][][]{testSet.get(i)});
            INDArray predictedOutput = model.output(newInput, false, null, labelsMask);

            double[][] features = testSet.get(i);
            normalizer.revertFeatures(features);

            double[][] newFeaturesArr = new double[4][];
            System.arraycopy(features, 0, newFeaturesArr, 0, 4);
            newFeaturesArr = Transposer.transpose(newFeaturesArr);

            double[][] real = testResult.get(i);
            normalizer.revertLabels(testSet.get(i), real);

            double[][] predMatrix = predictedOutput.slice(0).toDoubleMatrix();
            normalizer.revertLabels(testSet.get(i), predMatrix);

            double[][] predicted = new double[numOutputs][6];
            for (int j = 0; j < numOutputs; j++) {
                System.arraycopy(predMatrix[j], 0, predicted[j], 0, 6);
            }

            double[][] realFin = new double[numOutputs][6];
            for (int j = 0; j < numOutputs; j++) {
                System.arraycopy(real[j], 0, realFin[j], 0, 6);
            }
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
            double mean = realFin[2][0]*0.005;
            if (totalDifference < mean)
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
    }
}

