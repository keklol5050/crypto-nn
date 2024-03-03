package com.crypto.analysis.main;

import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.ArrayList;
import java.util.List;

public class LstmEx {

    public static void main(String[] args) {

        int numInputs = 5;
        int numOutputs = 1;
        int sequenceLength = 4;
        int batchSize = 4;
        int numEpochs = 20;

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Nesterovs(0.0001, 0.9))
                .list()
                .layer(0,new LSTM.Builder()
                        .nIn(numInputs).nOut(200)
                        .activation(Activation.TANH)
                        .build())
                .layer(1,new LSTM.Builder()
                        .nIn(200).nOut(400)
                        .activation(Activation.TANH)
                        .build())
                .layer(2,new LSTM.Builder()
                        .nIn(400).nOut(200)
                        .activation(Activation.TANH)
                        .build())
                .layer(3, new LSTM.Builder()
                        .nIn(200).nOut(128)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(4, new LSTM.Builder()
                        .nIn(128).nOut(96)
                        .activation(Activation.RELU)
                        .build())
                .layer(5, new LSTM.Builder()
                        .nIn(96).nOut(64)
                        .activation(Activation.TANH)
                        .build())
                .layer(6, new LSTM.Builder()
                        .nIn(64).nOut(32)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(7, new LSTM.Builder()
                        .nIn(32).nOut(16)
                        .activation(Activation.RELU)
                        .build())
                .layer(8, new LSTM.Builder()
                        .nIn(16).nOut(8)
                        .activation(Activation.TANH)
                        .build())
                .layer(9, new LSTM.Builder()
                        .nIn(8).nOut(4)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(10, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.TANH)
                        .nIn(4).nOut(numOutputs)
                        .build())
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(1));

        List<DataSet> sets = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            INDArray input = Nd4j.rand(new int[]{batchSize, numInputs, sequenceLength});
            INDArray labels = Nd4j.rand(new int[]{batchSize, numOutputs, 1});
            DataSet trainingData = new DataSet(input, labels);
            sets.add(trainingData);
        }
        DataSetIterator iterator = new ListDataSetIterator<>(sets, sets.size());

        for (int i = 0; i < numEpochs; i++) {
            net.fit(iterator);
        }

        INDArray newInput = Nd4j.rand(new int[]{1, numInputs, sequenceLength});
        INDArray predictedOutput = net.output(newInput);

        System.out.println("Predicted Output: " + predictedOutput);
    }
}