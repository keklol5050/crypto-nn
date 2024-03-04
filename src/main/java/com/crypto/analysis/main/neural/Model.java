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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;

public class Model {
    private final TrainDataSet trainSet;
    private final int numInputs;
    private final int numOutputs;
    private final int numEpochs;
    private final String pathToModel;
    private MultiLayerNetwork model;
    private DataSetIterator iterator;
    private NormalizerStandardize normalizer;
    private static final double LEARNING_RATE = 0.01;

    public Model(TrainDataSet trainSet, int numInputs, int numOutputs, int numEpochs, String pathToModel) {
        this.trainSet = trainSet;
        this.numInputs = numInputs;
        this.numOutputs = numOutputs;
        this.numEpochs = numEpochs;
        this.pathToModel = pathToModel;
        init();
    }

    public Model(TrainDataSet trainSet, int numInputs, int numOutputs, int numEpochs) {
        this.trainSet = trainSet;
        this.numInputs = numInputs;
        this.numOutputs = numOutputs;
        this.numEpochs = numEpochs;
        this.pathToModel = null;
        init();
    }


    public static void main(String[] args) {
        Model model = new Model(new TrainDataSet("BTCUSDT"), 30*20, 3, 10000, "D:\\model.zip");
        model.start();
    }

    private void init() {
        if (pathToModel!=null) {
            if (Files.exists(Path.of(pathToModel))) {
                model = ModelLoader.loadModel(pathToModel);
            } else {
                model = createModel();
            }
        } else model = createModel();

        iterator = getDataSetIterator();

        normalizer = new NormalizerStandardize();
        normalizer.fitLabel(true);

        normalizer.fit(iterator);
        iterator.setPreProcessor(normalizer);
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
            double predictionHigh = predictedOutput.getDouble(0);
            double predictionLow = predictedOutput.getDouble(1);
            double realLow = testResult.get(i)[0];
            double realHigh = testResult.get(i)[1];
            boolean isRight = (Math.abs(predictionLow - realLow) < 2) && (Math.abs(predictionHigh - realHigh) < 2);
            if (isRight) countRight++;
            System.out.printf("Predicted low: %f, real: %f, predicted high: %f, real: %f; is right (daily, 2%%) :%s ",
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
                .weightInit(WeightInit.XAVIER)
                .activation(Activation.TANH)
                .list()
                .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(6400).build())
                .layer(1, new DenseLayer.Builder().nIn(6400).nOut(6400).build())
                .layer(2, new DenseLayer.Builder().nIn(6400).nOut(3200).build())
                .layer(3, new DenseLayer.Builder().nIn(3200).nOut(1600).build())
                .layer(4, new DenseLayer.Builder().nIn(1600).nOut(800).build())
                .layer(5, new DenseLayer.Builder().nIn(800).nOut(400).build())
                .layer(6, new DenseLayer.Builder().nIn(400).nOut(200).build())
                .layer(7, new DenseLayer.Builder().nIn(200).nOut(128).build())
                .layer(8, new DenseLayer.Builder().nIn(128).nOut(64).build())
                .layer(9, new DenseLayer.Builder().nIn(64).nOut(32).build())
                .layer(10, new DenseLayer.Builder().nIn(32).nOut(16).build())
                .layer(11, new DenseLayer.Builder().nIn(16).nOut(8).build())
                .layer(12, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(8).nOut(numOutputs)
                        .build())
                .build();
    }
}
