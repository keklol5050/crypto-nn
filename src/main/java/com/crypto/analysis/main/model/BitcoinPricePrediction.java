package com.crypto.analysis.main.model;

import com.crypto.analysis.main.data.DataNormalizer;
import com.crypto.analysis.main.data.train.TrainDataSet;
import com.crypto.analysis.main.enumerations.Coin;
import com.crypto.analysis.main.enumerations.DataLength;
import com.crypto.analysis.main.enumerations.TimeFrame;
import com.crypto.analysis.main.ndata.CSVCoinDataSet;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.*;
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

        int numInputs = 30;
        int numOutputs = 3;
        int sequenceLength = 4;
        int batchSize = 1;
        int numEpochs = 200;
        CSVCoinDataSet setD = new CSVCoinDataSet(Coin.BTCUSDT, TimeFrame.FIFTEEN_MINUTES);
        setD.load();
        TrainDataSet trainSet = TrainDataSet.prepareTrainSet(Coin.BTCUSDT, DataLength.S30_3);
        DataNormalizer normalizer = trainSet.getNormalizer();

        LinkedList<double[][]> inputList = trainSet.getTrainData();
        LinkedList<double[][]> outputList = trainSet.getTrainResult();

        List<DataSet> sets = new ArrayList<>();
        for (int i = 0; i < inputList.size(); i++) {
            double[][] inputData = inputList.get(i);
            double[][] outputData = outputList.get(i);
            INDArray input = Nd4j.create(new int[]{batchSize, numInputs, sequenceLength});
            for (int k = 0; k < numInputs; k++) {
                for (int j = 0; j < sequenceLength; j++) {
                    input.putScalar(0, k, j, inputData[k][j]);
                }
            }
            INDArray labels = Nd4j.create(new int[]{batchSize, numOutputs, sequenceLength});
            for (int k = 0; k < numOutputs; k++) {
                for (int j = 0; j < sequenceLength; j++) {
                    labels.putScalar(0, k, j, outputData[k][j]);
                }
            }
            DataSet set = new DataSet(input, labels);
            sets.add(set);
        }

        Collections.shuffle(sets);
        DataSetIterator iterator = new ListDataSetIterator<>(sets, sets.size());

        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                .updater(new Adam(0.01))
                .list()
                .layer(0, new LSTM.Builder().nIn(numInputs).nOut(100)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(1, new DropoutLayer.Builder(0.25).nIn(100).nOut(80).build())
                .layer(2, new LSTM.Builder().nIn(80).nOut(80)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(3, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.IDENTITY)
                        .nIn(80).nOut(numOutputs)
                        .build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(config);
        model.init();

        System.out.println(model.summary());

        model.setListeners(new ScoreIterationListener(10));

            for (int i = 0; i < numEpochs; i++) {
            model.fit(iterator);
        }

        ModelLoader.saveModel(model, "D:\\model1.zip");


        LinkedList<double[][]> testSet = trainSet.getTestData();
        LinkedList<double[][]> testResult = trainSet.getTestResult();

        for (int i = 0; i < testSet.size(); i++) {
            INDArray newInput = Nd4j.create(new int[]{batchSize, numInputs, sequenceLength});
            double[][] inputData = testSet.get(i);
            normalizer.transform(inputData);
            for (int k = 0; k < numInputs; k++) {
                for (int j = 0; j < sequenceLength; j++) {
                    newInput.putScalar(0, k, j, inputData[k][j]);
                }
            }
            INDArray predictedOutput = model.output(newInput, false);
            System.out.println("Predicted: ");
            System.out.println(predictedOutput);
            System.out.println("Real: ");
            System.out.println(Arrays.deepToString(testResult.get(i)));
            System.out.println();
        }

        System.out.println("Time taken: " + ((System.currentTimeMillis() - start) / 1000) / 60 + " minutes");
    }
}