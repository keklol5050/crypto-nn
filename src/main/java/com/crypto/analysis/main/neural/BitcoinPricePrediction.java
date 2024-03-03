package com.crypto.analysis.main.neural;

import com.crypto.analysis.main.neural.train.TrainDataSet;
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
import org.nd4j.linalg.dataset.adapter.SingletonDataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.Arrays;
import java.util.LinkedList;

public class BitcoinPricePrediction {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        int numInputs = 600;
        int numOutputs = 20;
        int numEpochs = 10000;

        TrainDataSet trainSet = new TrainDataSet("BTCUSDT");
        trainSet.prepareTrainSet();

        LinkedList<double[][]> in = trainSet.getTrainData();
        LinkedList<double[]> out = trainSet.getTrainResult();

        LinkedList<double[]> inputList = new LinkedList<>();

        for (double[][] inArr : in) {
            inputList.add(Arrays.stream(inArr).flatMapToDouble(Arrays::stream).toArray());
        }

        int dataSize = inputList.size();
        INDArray inputArray = Nd4j.create(new double[dataSize][numInputs]);
        INDArray outputArray = Nd4j.create(new double[dataSize][numOutputs]);

        for (int i = 0; i < dataSize; i++) {
            for (int j = 0; j < numInputs; j++) {
                inputArray.putScalar(i, j, inputList.get(i)[j]);
            }

            double[] outArr = out.get(i);
            for (int j = 0; j < numOutputs; j++) {
                outputArray.putScalar(i, j, outArr[j + 1]);
            }
        }

        DataSet dataSet = new DataSet(inputArray, outputArray);

        DataSetIterator iterator = new SingletonDataSetIterator(dataSet);

        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Nesterovs(0.001, 0.9))
                .l2(0.00001)
                .list()
                .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(3200)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(3200).nOut(3200)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(2, new DenseLayer.Builder().nIn(3200).nOut(1600)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                .layer(3, new DenseLayer.Builder().nIn(1600).nOut(800)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(4, new DenseLayer.Builder().nIn(800).nOut(400)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                .layer(5, new DenseLayer.Builder().nIn(400).nOut(200)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(6, new DenseLayer.Builder().nIn(200).nOut(100)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                .layer(7, new DenseLayer.Builder().nIn(100).nOut(200)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(8, new DenseLayer.Builder().nIn(200).nOut(400)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(9, new DenseLayer.Builder().nIn(400).nOut(800)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(10, new DenseLayer.Builder().nIn(800).nOut(1600)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(11, new DenseLayer.Builder().nIn(1600).nOut(3200)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(12, new DenseLayer.Builder().nIn(3200).nOut(3200)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(13, new DenseLayer.Builder().nIn(3200).nOut(1600)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(14, new DenseLayer.Builder().nIn(1600).nOut(800)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(15, new DenseLayer.Builder().nIn(800).nOut(400)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(16, new DenseLayer.Builder().nIn(400).nOut(200)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(17, new DenseLayer.Builder().nIn(200).nOut(100)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(18, new DenseLayer.Builder().nIn(100).nOut(64)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(19, new DenseLayer.Builder().nIn(64).nOut(32)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(20, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.IDENTITY)
                        .nIn(32).nOut(numOutputs)
                        .build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(config);
        model.init();
        model.setListeners(new ScoreIterationListener(10));

        System.out.println(model.summary());

        for (int i = 0; i < numEpochs; i++) {
            model.fit(iterator);
        }

        ModelLoader.saveModel(model, "D:\\model.zip");

        LinkedList<double[][]> testSet = trainSet.getTestData();
        LinkedList<double[]> testResult = trainSet.getTestResult();
        int countRight = 0;

        for (int i = 0; i < testSet.size(); i++) {
            INDArray newInput = Nd4j.create(new double[][]{Arrays.stream(testSet.get(i)).flatMapToDouble(Arrays::stream).toArray()});
            INDArray predictedOutput = model.output(newInput, false);
            double predictionHigh = predictedOutput.getDouble(0);
            double predictionLow = predictedOutput.getDouble(1);
            double realLow = testResult.get(i)[1];
            double realHigh = testResult.get(i)[2];
            boolean isRight = (Math.abs(predictionLow - realLow) < 200) && (Math.abs(predictionHigh - realHigh) < 200);
            if (isRight) countRight++;
            System.out.printf("Predicted low: %f, real: %f, predicted high: %f, real: %f; is right (daily, 200) :%s ",
                    predictionLow, realLow, predictionHigh, realHigh, isRight);
            System.out.println();
        }

        System.out.println("Right: " + countRight + " from " + testSet.size());
        System.out.println("Percentage: " + ((double) countRight / (double) testSet.size()) * 100 + '%');
        System.out.println("Time taken: " + ((System.currentTimeMillis() - start) / 1000) / 60 + " minutes");
    }
}