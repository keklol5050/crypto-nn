package com.crypto.analysis.main.core.classification;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.model.ModelLoader;
import com.crypto.analysis.main.core.ndata.CSVCoinDataSet;
import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.InvocationType;
import org.deeplearning4j.optimize.listeners.EvaluativeListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.model.stats.StatsListener;
import org.deeplearning4j.ui.model.storage.FileStatsStorage;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;

public class Model {
    public static void main(String[] args) {
        CSVCoinDataSet cs = new CSVCoinDataSet(Coin.BTCUSDT, TimeFrame.ONE_HOUR);
        cs.load();

        int numEpochs = 600;
        TrainDataSet trainDataSet = TrainDataSet.prepareTrainSet(Coin.BTCUSDT, cs);

        ListDataSetIterator<DataSet> trainIterator = trainDataSet.getTrainIterator();
        ListDataSetIterator<DataSet> testIterator = trainDataSet.getTestIterator();

        NormalizerStandardize normalizerStandardize = new NormalizerStandardize();
        normalizerStandardize.fit(trainIterator);

        trainIterator.setPreProcessor(normalizerStandardize);
        testIterator.setPreProcessor(normalizerStandardize);

        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(123)
                .trainingWorkspaceMode(WorkspaceMode.ENABLED)
                .inferenceWorkspaceMode(WorkspaceMode.ENABLED)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                .weightInit(WeightInit.LECUN_NORMAL)
                .activation(Activation.TANH)
                .l1(0.000128)
                .l2(0.000128)
                .updater(new Adam(0.005))
                .list()
                .layer(0, new LSTM.Builder()
                        .nIn(trainDataSet.getCountInput())
                        .nOut(100)
                        .build())
                .layer(1, new LSTM.Builder()
                        .nIn(100)
                        .nOut(100)
                        .build())
                .layer(2, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                        .activation(Activation.SOFTMAX)
                        .nIn(100)
                        .nOut(trainDataSet.getCountOutput())
                        .build())
                .backpropType(BackpropType.Standard)
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(config);
        model.init();

        System.out.println(model.summary());

       UIServer uiServer = UIServer.getInstance();
        StatsStorage statsStorage = new FileStatsStorage(new File(System.getProperty("java.io.tmpdir"), "ui-stats.dl4j"));

        model.setListeners(
                new StatsListener(statsStorage, 10),
                new ScoreIterationListener(10),
                new EvaluativeListener(testIterator, 10, InvocationType.EPOCH_START)
        );

        uiServer.attach(statsStorage);
        System.gc();
        for (int i = 0; i < numEpochs; i++) {
            if (i % 50 == 0 && i > 0) {
                ModelLoader.saveModel(model, "D:\\model2classification.zip");
                System.out.println("Saved at epoch: " + model.getEpochCount());
            }
            model.fit(trainIterator);
            System.gc();
        }

        ModelLoader.saveModel(model, "D:\\model2classification.zip");
        int countRight = 0;
        testIterator.reset();
        while (testIterator.hasNext()) {
            DataSet set = testIterator.next(1);
            INDArray output = model.output(set.getFeatures(), false, null, trainDataSet.getLabelsMask());
            double[][] outputMatrix = output.slice(0).toDoubleMatrix();
            double maxValue = 0;
            int pClass = 0;
            for (int i = 0; i < outputMatrix.length; i++) {
                double[] d = outputMatrix[i];
                maxValue = Math.max(maxValue, d[d.length-1]);
                if (maxValue == d[d.length-1]) pClass = i;
            }
            int rClass = 0;
            double[][] real = set.getLabels().slice(0).toDoubleMatrix();
            for (int i = 0; i < real.length; i++) {
                if (real[i][real[i].length-1] == 1) rClass = i;
            }
            System.out.println("Real class: " + rClass);
            System.out.println("Predicted class: " + pClass);
            System.out.println();
            if (pClass==rClass) countRight++;
        }
        System.out.println("Count right: " + countRight);
    }
}
