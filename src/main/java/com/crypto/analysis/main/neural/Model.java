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
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.Arrays;
import java.util.LinkedList;

public class Model {
    private final TrainDataSet trainSet;
    private final int numInputs;
    private final int numOutputs;
    private final int numEpochs;
    private final String pathToModel;
    private MultiLayerNetwork model;
    private DataSet dataSet;
    private DataSetIterator iterator;
    private NormalizerStandardize normalizer;
    private static final double LEARNING_RATE = 0.001;

    public Model(TrainDataSet trainSet, int numInputs, int numOutputs, int numEpochs, String pathToModel) {
        this.trainSet = trainSet;
        this.numInputs = numInputs;
        this.numOutputs = numOutputs;
        this.numEpochs = numEpochs;
        this.pathToModel = pathToModel;
        init();
    }


    public static void main(String[] args) {
        Model model = new Model(new TrainDataSet("BTCUSDT"), 30*20, 20, 10000, "D:\\model.zip");
        model.start();
    }

    private void init() {
        model = createModel();
        iterator = getDataSetIterator();

        normalizer = new NormalizerStandardize();
        normalizer.fitLabel(true);

        normalizer.fit(dataSet);
        normalizer.transform(dataSet);
    }


    public void start() {
        long start = System.currentTimeMillis();

        System.out.println(model.summary());
        for (int i = 0; i < numEpochs; i++) {
            model.fit(iterator);
        }

        ModelLoader.saveModel(model, pathToModel);

        testModel();

        System.out.println("Time taken: " + ((System.currentTimeMillis() - start) / 1000) / 60 + " minutes");
    }

    private void testModel() {
        LinkedList<double[][]> testSet = trainSet.getTestData();
        LinkedList<double[]> testResult = trainSet.getTestResult();
        int countRight = 0;

        for (int i = 0; i < testSet.size(); i++) {
            INDArray newInput = Nd4j.create(new double[][]{Arrays.stream(testSet.get(i)).flatMapToDouble(Arrays::stream).toArray()});
            normalizer.transform(newInput);
            INDArray predictedOutput = model.output(newInput, false);
            normalizer.revertLabels(predictedOutput);
            double predictionHigh = predictedOutput.getDouble(17);
            double predictionLow = predictedOutput.getDouble(18);
            double realLow = testResult.get(i)[17];
            double realHigh = testResult.get(i)[18];
            boolean isRight = (Math.abs(predictionLow - realLow) < 200) && (Math.abs(predictionHigh - realHigh) < 200);
            if (isRight) countRight++;
            System.out.printf("Predicted low: %f, real: %f, predicted high: %f, real: %f; is right (daily, 200) :%s ",
                    predictionLow, realLow, predictionHigh, realHigh, isRight);
            System.out.println();
        }

        System.out.println("Right: " + countRight + " from " + testSet.size());
        System.out.println("Percentage: " + ((double) countRight / (double) testSet.size()) * 100 + '%');
    }


    private DataSetIterator getDataSetIterator() {
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
                outputArray.putScalar(i, j, outArr[j]);
            }
        }

        DataSet dataSet = new DataSet(inputArray, outputArray);
        this.dataSet = dataSet;
        return new SingletonDataSetIterator(dataSet);
    }

    private MultiLayerNetwork createModel() {
        MultiLayerNetwork model = new MultiLayerNetwork(getConfiguration());
        model.init();
        model.setListeners(new ScoreIterationListener(10));
        return model;
    }


    private MultiLayerConfiguration getConfiguration() {
        return new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(LEARNING_RATE))
                .list()
                .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(800)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.TANH)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(800).nOut(800)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.TANH)
                        .build())
                .layer(2, new DenseLayer.Builder().nIn(800).nOut(400)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.TANH)
                        .build())
                .layer(3, new DenseLayer.Builder().nIn(400).nOut(200)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.TANH)
                        .build())
                .layer(4, new DenseLayer.Builder().nIn(200).nOut(100)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.TANH)
                        .build())
                .layer(5, new DenseLayer.Builder().nIn(100).nOut(64)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.TANH)
                        .build())
                .layer(6, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.IDENTITY)
                        .nIn(64).nOut(numOutputs)
                        .build())
                .build();
    }
}
