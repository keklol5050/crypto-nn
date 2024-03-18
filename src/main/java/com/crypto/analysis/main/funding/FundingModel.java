package com.crypto.analysis.main.funding;

import com.crypto.analysis.main.data_utils.enumerations.Coin;
import com.crypto.analysis.main.model.ModelLoader;
import org.deeplearning4j.datasets.iterator.JointMultiDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.RNNFormat;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.preprocessor.MultiNormalizerMinMaxScaler;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.nio.file.Files;
import java.nio.file.Path;

public class FundingModel {
    private final Coin coin;

    private final int numEpochs;
    private final int batchSize;
    private int numInputsSingleClassification;
    private int numOutputsClassification;
    private int numInputsMultipleClassification;
    private int numInputsRegression;
    private int sequenceLengthMultiClassification;
    private int sequenceLengthRegression;
    private static final double LEARNING_RATE = 0.01;

    private final String pathToModel;
    private final String pathToNormalizer;

    private ComputationGraph model;
    private JointMultiDataSetIterator iterator;
    private MultiNormalizerMinMaxScaler normalizer;

    public FundingModel(Coin coin, int numEpochs, int batchSize, String pathToModel, String pathToNormalizer) throws Exception {
        this.coin = coin;
        this.numEpochs = numEpochs;
        this.batchSize = batchSize;

        this.pathToModel = pathToModel;
        this.pathToNormalizer = pathToNormalizer;
        init();
    }

    public FundingModel(Coin coin, int numEpochs, int batchSize) throws Exception {
        this.coin = coin;
        this.numEpochs = numEpochs;
        this.batchSize = batchSize;

        this.pathToModel = null;
        this.pathToNormalizer = null;
        init();
    }


    public static void main(String[] args) throws Exception {
        FundingModel model = new FundingModel(Coin.BTCUSDT, 1000, 1);
        model.start();
    }

    private void init() throws Exception {
        FundingTrainSet set = FundingTrainSetFactory.getInstance(coin, 10, batchSize);

        iterator = set.getIterator();
        normalizer = set.getNormalizer();

        numInputsSingleClassification = set.getNumInputsSingleClassification();
        numOutputsClassification = set.getNumOutputsClassification();

        numInputsMultipleClassification = numInputsRegression = set.getCapacity();
        sequenceLengthMultiClassification = set.getSequenceLengthMultiClassification();
        sequenceLengthRegression = set.getSequenceLengthRegression();

        if (pathToModel != null && pathToNormalizer != null) {
            if (Files.exists(Path.of(pathToModel)) && Files.exists(Path.of(pathToNormalizer))) {
                model = ModelLoader.loadGraph(pathToModel);
            } else {
                model = createModel();
            }
        } else model = createModel();
        assert model != null;
        model.setListeners(new ScoreIterationListener(10));

    }


    public void start() throws Exception {
        long start = System.currentTimeMillis();

        System.out.println(model.summary());
        for (int i = 0; i < numEpochs; i++) {
            model.fit(iterator);
            if (i > 0 && i % 1000 == 0 && pathToModel != null) {
                ModelLoader.saveModel(model, pathToModel);
            }
        }

        if (pathToModel != null) {
            ModelLoader.saveModel(model, pathToModel);
        }

        // testModel();

        System.out.println("Time taken: " + ((System.currentTimeMillis() - start) / 1000) / 60 + " minutes");
    }


    private ComputationGraph createModel() {
        ComputationGraph model = new ComputationGraph(getConfiguration());
        model.init();
        return model;
    }


    private ComputationGraphConfiguration getConfiguration() {
        return new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                .weightInit(WeightInit.XAVIER)
                .activation(Activation.LEAKYRELU)
                .updater(new Nesterovs(LEARNING_RATE, 0.9))
                .graphBuilder()
                .setInputTypes(InputType.feedForward(numInputsSingleClassification),
                        InputType.recurrent(numInputsMultipleClassification, sequenceLengthMultiClassification, RNNFormat.NCW),
                        InputType.recurrent(numInputsRegression, sequenceLengthRegression, RNNFormat.NCW))
                .addInputs("single_classification",
                        "multiple_classification", "regression")

                // Layer 1
                .addLayer("single_1", new DenseLayer.Builder()
                        .nIn(numInputsSingleClassification)
                        .nOut(64)
                        .build(), "single_classification")
                .addLayer("single_2", new DenseLayer.Builder()
                        .nIn(64)
                        .nOut(128)
                        .build(), "single_1")
                .addLayer("single_3", new DenseLayer.Builder()
                        .nIn(128)
                        .nOut(128)
                        .build(), "single_2")
                .addLayer("single_4", new DropoutLayer.Builder()
                        .nIn(128)
                        .nOut(128)
                        .dropOut(0.2)
                        .build(), "single_3")
                .addLayer("single_5", new DenseLayer.Builder()
                        .nIn(128)
                        .nOut(64)
                        .build(), "single_4")
                .addLayer("output_single", new OutputLayer.Builder()
                        .nIn(64)
                        .nOut(numOutputsClassification)
                        .activation(Activation.SOFTMAX)
                        .lossFunction(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .build(), "single_5")

                // Layer 2
                .addLayer("multiple_1", new LSTM.Builder()
                        .nIn(numInputsMultipleClassification)
                        .nOut(32)
                        .build(), "multiple_classification")
                .addLayer("output_multiple", new RnnOutputLayer.Builder()
                        .nIn(32)
                        .nOut(numOutputsClassification)
                        .activation(Activation.SOFTMAX)
                        .lossFunction(LossFunctions.LossFunction.MCXENT)
                        .build(), "multiple_1")

                // Layer 3
                .addLayer("regression_1", new LSTM.Builder()
                        .nIn(numInputsRegression)
                        .nOut(32)
                        .build(), "regression")
                .addLayer("output_regression", new RnnOutputLayer.Builder()
                        .nIn(32)
                        .nOut(1)
                        .activation(Activation.IDENTITY)
                        .lossFunction(LossFunctions.LossFunction.MSE)
                        .build(), "regression_1")

                .setOutputs("output_single", "output_multiple", "output_regression")
                .build();
    }
}
