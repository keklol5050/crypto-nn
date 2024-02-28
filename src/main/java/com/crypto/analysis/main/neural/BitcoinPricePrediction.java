package com.crypto.analysis.main.neural;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.deeplearning4j.datasets.iterator.utilty.SingletonDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
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

import java.util.List;

public class BitcoinPricePrediction {

    public static void main(String[] args) throws JsonProcessingException {
        int numInput = 510;

        int numOutput = 1;
        int numEpochs = 20000;
        TrainDataSet trainSet = new TrainDataSet("BTCUSDT");
        trainSet.prepareTrainSet();

        List<double[]> inputList = trainSet.getFinalTrainSet();
        List<Double> outputList = trainSet.getFinalTrainResult();


        int dataSize = inputList.size();
        INDArray inputArray = Nd4j.create(new double[dataSize][numInput]);
        INDArray outputArray = Nd4j.create(new double[dataSize][1]);

        for (int i = 0; i < dataSize; i++) {
            for (int j = 0; j < numInput; j++) {
                inputArray.putScalar(i, j, inputList.get(i)[j]);
            }
            outputArray.putScalar(i, 0, outputList.get(i));
        }

        DataSet dataSet = new DataSet(inputArray, outputArray);
        DataSetIterator iterator = new SingletonDataSetIterator(dataSet);

        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(0.001))
                .list()
                .layer(0, new DenseLayer.Builder().nIn(numInput).nOut(256)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(256).nOut(128)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new DenseLayer.Builder().nIn(128).nOut(64)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                .layer(3, new DenseLayer.Builder().nIn(64).nOut(32)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                .layer(4, new DenseLayer.Builder().nIn(32).nOut(16)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.IDENTITY)
                        .nIn(16).nOut(numOutput).build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(config);
        model.init();
        model.setListeners(new ScoreIterationListener(10));

        for (int epoch = 0; epoch < numEpochs; epoch++) {
            model.fit(iterator);
        }

        List<double[]> testSet = trainSet.getFinalTestSet();
        List<Double> testResult = trainSet.getFinalTestResult();
        int countRight = 0;

        for (int i = 0; i < testSet.size(); i++) {
            INDArray newInput = Nd4j.create(new double[][]{testSet.get(i)});
            INDArray predictedOutput = model.output(newInput, false);
            double prediction = predictedOutput.getDouble(0) * 10000;
            double real = testResult.get(i) * 10000;
            boolean isRight = Math.abs(prediction - real) < 100;
            if (isRight) countRight++;
            System.out.printf("Predicted: %f, Real: %f, is right (100) :%s ", prediction, real, isRight);
            System.out.println();
        }
        System.out.println("Right: "+countRight + " from " + testSet.size());
        System.out.println("Percentage: " + ((double) countRight/(double) testSet.size()) * 100 + '%');

    }

}