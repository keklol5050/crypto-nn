package com.crypto.analysis.main.model;

import com.crypto.analysis.main.data.train.TrainDataSet;
import com.crypto.analysis.main.data_utils.normalizers.BatchNormalizer;
import com.crypto.analysis.main.data_utils.enumerations.Coin;
import com.crypto.analysis.main.data_utils.enumerations.DataLength;
import com.crypto.analysis.main.data_utils.enumerations.TimeFrame;
import com.crypto.analysis.main.ndata.CSVCoinDataSet;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
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
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.*;

public class BitcoinPricePrediction {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        DataLength length = DataLength.S50_3;
        CSVCoinDataSet setD = new CSVCoinDataSet(Coin.BTCUSDT, TimeFrame.FIFTEEN_MINUTES);
        setD.load();
        TrainDataSet trainSet = TrainDataSet.prepareTrainSet(Coin.BTCUSDT, length, setD);

        LinkedList<double[][]> inputList = trainSet.getTrainData();
        LinkedList<double[][]> outputList = trainSet.getTrainResult();

        int numInputs = inputList.get(0).length;
        int numOutputs = outputList.get(0).length;
        int sequenceLength = inputList.get(0)[0].length;
        int numEpochs = 1000;

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

        DataSetIterator iterator = new ListDataSetIterator<>(sets, 3072);
        BatchNormalizer normalizer = trainSet.getNormalizer();
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                .l2(5e-5)
                .weightInit(WeightInit.XAVIER)
                .activation(Activation.LEAKYRELU)
                .updater(new Adam(0.01))
                .list()
                .layer(0, new LSTM.Builder().nIn(numInputs).nOut(256).build())
                .layer(1, new LSTM.Builder().nIn(256).nOut(256).build())
                .layer(2, new LSTM.Builder().nIn(256).nOut(256).build())
                .layer(3, new LSTM.Builder().nIn(256).nOut(128).build())
                .layer(4, new LSTM.Builder().nIn(128).nOut(64).build())
                .layer(5, new LSTM.Builder().nIn(64).nOut(32).build())
                .layer(6, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(32).nOut(numOutputs)
                        .build())
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
           if (i % 100 == 0 && i > 0){
                try {
                    Thread.sleep(30000);
                    System.gc();
                    System.gc();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (i % 20 == 0 && i > 0)
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

            double[][] real = testResult.get(i);
            normalizer.revertLabelsVertical(testSet.get(i), real);

            double[][] predMatrix = predictedOutput.slice(0).toDoubleMatrix();
            normalizer.revertLabelsVertical(testSet.get(i), predMatrix);

            double[][] realFin = {
                    {real[0][0], real[0][1], real[0][2]},
                    {real[1][0], real[1][1], real[1][2]},
                    {real[2][0], real[2][1], real[2][2]}
            };

            double[][] predicted = {
                    {predMatrix[0][0], predMatrix[0][1], predMatrix[0][2]},
                    {predMatrix[1][0], predMatrix[1][1], predMatrix[1][2]},
                    {predMatrix[2][0], predMatrix[2][1], predMatrix[2][2]}
            };

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
            System.out.println("Predicted: ");
            System.out.println(Arrays.deepToString(predicted));
            System.out.println("Real: ");
            System.out.println(Arrays.deepToString(realFin));
            System.out.println("Total difference " + totalDifference + ", mean: " + mean);
            System.out.println();
        }
        System.out.println("Percentage: " + ((double) countRight / (double) testSet.size()) * 100 + '%');
        System.out.println("Time taken: " + ((System.currentTimeMillis() - start) / 1000) / 60 + " minutes");
    }
}