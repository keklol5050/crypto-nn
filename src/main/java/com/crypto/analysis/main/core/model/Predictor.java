package com.crypto.analysis.main.core.model;

import com.crypto.analysis.main.core.data.train.TrainDataSet;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.ndata.CSVCoinDataSet;
import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.conf.layers.recurrent.Bidirectional;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.model.stats.StatsListener;
import org.deeplearning4j.ui.model.storage.FileStatsStorage;
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Predictor {
    public static void main(String[] args) {

        DataLength length = DataLength.L60_6;
        TimeFrame tf = TimeFrame.ONE_HOUR;

        CSVCoinDataSet setD = new CSVCoinDataSet(Coin.BTCUSDT, tf);
        setD.load();

        TrainDataSet trainSet = TrainDataSet.prepareTrainSet(Coin.BTCUSDT, length, setD);

        LinkedList<double[][]> trainInput = trainSet.getTrainData();
        LinkedList<double[][]> trainOutput = trainSet.getTrainResult();

        LinkedList<double[][]> testInput = trainSet.getTestData();
        LinkedList<double[][]> testOutput = trainSet.getTestResult();

        int numInputs = trainInput.get(0).length;
        int numOutputs = trainOutput.get(0).length;
        int sequenceLength = trainInput.get(0)[0].length;
        int numEpochs = 1000;

        INDArray labelsMask = Nd4j.zeros(1, sequenceLength);
        for (int i = 0; i < length.getCountOutput(); i++) {
            labelsMask.putScalar(new int[]{0, i}, 1.0);
        }

        List<DataSet> trainSets = new ArrayList<>();
        for (int i = 0; i < trainInput.size(); i++) {
            double[][] inputData = trainInput.get(i);
            double[][] outputData = trainOutput.get(i);

            INDArray input = Nd4j.createFromArray(new double[][][]{inputData});
            INDArray labels = Nd4j.createFromArray(new double[][][]{outputData});

            DataSet set = new DataSet(input, labels, null, labelsMask);
            trainSets.add(set);
        }

        List<DataSet> validationSets = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            double[][] inputData = testInput.get(i);
            double[][] outputData = testOutput.get(i);

            INDArray input = Nd4j.createFromArray(new double[][][]{inputData});
            INDArray labels = Nd4j.createFromArray(new double[][][]{outputData});

            DataSet set = new DataSet(input, labels, null, labelsMask);
            validationSets.add(set);
        }

        Collections.shuffle(trainSets);
        Collections.shuffle(validationSets);

        DataSetIterator trainIterator = new ListDataSetIterator<>(trainSets, 64);
        DataSetIterator validationIterator = new ListDataSetIterator<>(validationSets, 64);

        NormalizerStandardize normalizerStandardize = new NormalizerStandardize();
        normalizerStandardize.fitLabel(true);
        normalizerStandardize.fit(trainIterator);

        trainIterator.setPreProcessor(normalizerStandardize);
        validationIterator.setPreProcessor(normalizerStandardize);

        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(123)
                .trainingWorkspaceMode(WorkspaceMode.ENABLED)
                .inferenceWorkspaceMode(WorkspaceMode.ENABLED)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                .weightInit(WeightInit.LECUN_NORMAL)
                .activation(Activation.TANH)
                .l1(0.000128)
                .l2(0.000432)
                .updater(new Adam(0.00256))
                .list()
                .setInputType(InputType.recurrent(numInputs, sequenceLength, RNNFormat.NCW))
                .layer(0, new Bidirectional(Bidirectional.Mode.CONCAT,
                        new LSTM.Builder()
                                .nIn(numInputs)
                                .nOut(800)
                                .dropOut(0.944)
                                .build()))
                .layer(1, new Bidirectional(Bidirectional.Mode.CONCAT,
                        new LSTM.Builder()
                                .nOut(400)
                                .dropOut(0.800)
                                .build()))
                .layer(2, new Bidirectional(Bidirectional.Mode.CONCAT,
                        new LSTM.Builder()
                                .nOut(400)
                                .dropOut(0.800)
                                .build()))
                .layer(3, new Bidirectional(Bidirectional.Mode.CONCAT,
                        new LSTM.Builder()
                                .nOut(200)
                                .dropOut(0.912)
                                .build()))
                .layer(4, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nOut(numOutputs)
                        .build())
                .backpropType(BackpropType.Standard)
                .build();

        MultiLayerNetwork model = ModelLoader.loadNetwork("D:\\model20.zip");
        model.init();

        System.out.println(model.summary());

        System.out.println("Count input params: " + numInputs);
        System.out.println("Count output params: " + numOutputs);
        System.out.println("Count input objects: " + sequenceLength);
        System.out.println("Train set size: " + trainSets.size());
        System.out.println("Validation set size: " + validationSets.size());
        System.out.println();

        UIServer uiServer = UIServer.getInstance();
        StatsStorage statsStorage = new FileStatsStorage(new File(System.getProperty("java.io.tmpdir"), "ui-stats.dl4j"));

        model.setListeners(new StatsListener(statsStorage, 10), new ScoreIterationListener(10));
        uiServer.attach(statsStorage);

        System.gc();

        //  double testMSEBest = 10;

        for (int i = 0; i < numEpochs; i++) {
            if (i % 5 == 0 && i > 0) {
                RegressionEvaluation eval = new RegressionEvaluation();
                System.out.println("Evaluating validation set...");
                while (validationIterator.hasNext()) {
                    DataSet set = validationIterator.next(1);
                    INDArray output = model.output(set.getFeatures());
                    eval.eval(set.getLabels(), output, labelsMask);
                }
                validationIterator.reset();
                System.out.println(eval.stats());
                System.out.println();

            /*    if (eval.meanSquaredError(0) < testMSEBest) {
                    ModelLoader.saveModel(model, "D:\\model18best.zip");
                    System.out.println("Saved as best at epoch: " + model.getEpochCount());
                    testMSEBest = eval.meanSquaredError(0);
                }
                System.out.println("Best validation MSE: " + testMSEBest);
                System.out.println("Current validation MSE: " + eval.meanSquaredError(0));
            */

                ModelLoader.saveModel(model, "D:\\model20.zip");
                System.out.println("Saved at epoch: " + model.getEpochCount());
            }
            model.fit(trainIterator);
            System.gc();
        }

        ModelLoader.saveModel(model, "D:\\model20.zip");
    }
}

