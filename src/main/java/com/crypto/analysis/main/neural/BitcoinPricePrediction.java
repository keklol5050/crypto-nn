package com.crypto.analysis.main.neural;

import com.crypto.analysis.main.neural.train.TrainDataSet;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.datasets.iterator.utilty.SingletonDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.WorkspaceMode;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.learning.config.RmsProp;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BitcoinPricePrediction {

    public static void main(String[] args) throws JsonProcessingException {
        long start = System.currentTimeMillis();

        int numInputs = 20;
        int numOutputs = 1;
        int sequenceLength = 4;
        int batchSize = 1;
        int numEpochs = 10000;

        TrainDataSet trainSet = new TrainDataSet("BTCUSDT");
        trainSet.prepareTrainSet();

        LinkedList<double[][]> inputList = trainSet.getTrainData();
        LinkedList<double[]> outputList = trainSet.getTrainResult();

        List<DataSet> sets = new ArrayList<>();
        for (int i = 0; i < inputList.size(); i++) {
            double[][] inputData = inputList.get(i);
            double[] outputData = outputList.get(i);
            INDArray input = Nd4j.create(new int[]{batchSize, numInputs, sequenceLength});
            for (int k = 0; k < numInputs; k++) {
                for (int j = 0; j < sequenceLength; j++) {
                    input.putScalar(0, k, j, inputData[k][j]);
                }
            }
            INDArray labels = Nd4j.create(new int[]{batchSize, numOutputs, sequenceLength});
            for (int k = 0; k < sequenceLength; k++) {
                labels.putScalar(0, 0, k, outputData[k]);
            }
            DataSet set = new DataSet(input, labels);
            sets.add(set);
        }

        DataSetIterator iterator = new ListDataSetIterator<>(sets, sets.size());

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
                        .activation(Activation.IDENTITY)
                        .nIn(4).nOut(numOutputs)
                        .build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(10));

        for (int i = 0; i < numEpochs; i++) {
            model.fit(iterator);
        }

        LinkedList<double[][]> testSet = trainSet.getTestData();
        LinkedList<double[]> testResult = trainSet.getTestResult();
        int countRight = 0;

        for (int i = 0; i < testSet.size(); i++) {
            INDArray newInput = Nd4j.create(new int[]{1, numInputs, sequenceLength});
            double[][] inputData = testSet.get(i);
            for (int k = 0; k < numInputs; k++) {
                for (int j = 0; j < sequenceLength; j++) {
                    newInput.putScalar(0, k, j, inputData[k][j]);
                }
            }
            INDArray predictedOutput = model.output(newInput, false);
            double predictionLow = predictedOutput.getDouble(2);
            double predictionHigh = predictedOutput.getDouble(3);
            double realLow = testResult.get(i)[2];
            double realHigh = testResult.get(i)[3];
            boolean isRight = (Math.abs(predictionLow - realLow) < 200) && (Math.abs(predictionHigh - realHigh) < 200);
            if (isRight) countRight++;
            System.out.printf("Predicted low: %f, high: %f, Real low: %f, Real high: %f, is right(200): %s",
                    predictionLow, predictionHigh, realLow, realHigh, isRight);
            System.out.println();
        }
        System.out.println("Right: "+countRight + " from " + testSet.size());
        System.out.println("Percentage: " + ((double) countRight/(double) testSet.size()) * 100 + '%');
        System.out.println("Time taken: " + ((System.currentTimeMillis()-start)/1000) + " seconds");
    }

}