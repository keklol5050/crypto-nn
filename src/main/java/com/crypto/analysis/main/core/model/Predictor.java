package com.crypto.analysis.main.core.model;

import com.crypto.analysis.main.core.data_utils.train.TrainDataSet;
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
import org.nd4j.jita.conf.CudaEnvironment;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;

public class Predictor {
    public static void main(String[] args) {
        int numEpochs = 1000;
        DataLength length = DataLength.L60_6;
        TimeFrame tf = TimeFrame.ONE_HOUR;

        CSVCoinDataSet setD = new CSVCoinDataSet(Coin.BTCUSDT, tf);
        setD.load();

        TrainDataSet trainDataSet = TrainDataSet.prepareTrainSet(Coin.BTCUSDT, length, setD);
        ListDataSetIterator<DataSet> trainIterator = trainDataSet.getTrainIterator();
        ListDataSetIterator<DataSet> testIterator = trainDataSet.getTestIterator();

        INDArray labelsMask = trainDataSet.getLabelsMask();

        NormalizerStandardize normalizerStandardize = new NormalizerStandardize();
        normalizerStandardize.fitLabel(true);
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
                .cacheMode(CacheMode.DEVICE)
                .l2(1e-3)
                .updater(new Adam())
                .list()
                .setInputType(InputType.recurrent(trainDataSet.getCountInput(), trainDataSet.getSequenceLength(), RNNFormat.NCW))
                .layer(0, new Bidirectional(Bidirectional.Mode.CONCAT,
                        new LSTM.Builder()
                                .nIn(trainDataSet.getCountInput())
                                .nOut(256)
                                .build()))
                .layer(1, new Bidirectional(Bidirectional.Mode.CONCAT,
                        new LSTM.Builder()
                                .nOut(256)
                                .build()))
                .layer(2, new Bidirectional(Bidirectional.Mode.CONCAT,
                        new LSTM.Builder()
                                .nOut(256)
                                .build()))
                .layer(3, new Bidirectional(Bidirectional.Mode.CONCAT,
                        new LSTM.Builder()
                                .nOut(256)
                                .build()))
                .layer(3, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nOut(trainDataSet.getCountOutput())
                        .build())
                .backpropType(BackpropType.Standard)
                .build();

        MultiLayerNetwork model = ModelLoader.loadNetwork("D:\\model2.zip");
        model.init();
        CudaEnvironment.getInstance().getConfiguration().setMaximumDeviceCacheableLength(1024 * 1024 * 2048L).setMaximumDeviceCache((long) (0.5 * 6096 * 1024 * 1024 * 2048L)).setMaximumHostCacheableLength(1024 * 1024 * 2048L).setMaximumHostCache((long) (0.5 * 6096 * 1024 * 1024 * 2048L));
        Nd4j.getMemoryManager().setAutoGcWindow(50000);

        System.out.println(model.summary());

        System.out.println("Count input params: " + trainDataSet.getCountInput());
        System.out.println("Count output params: " + trainDataSet.getCountOutput());
        System.out.println("Count input objects: " + trainDataSet.getSequenceLength());

        System.out.println();

        UIServer uiServer = UIServer.getInstance();
        StatsStorage statsStorage = new FileStatsStorage(new File(System.getProperty("java.io.tmpdir"), "ui-stats.dl4j"));

        model.setListeners(new StatsListener(statsStorage, 10), new ScoreIterationListener(10));
        uiServer.attach(statsStorage);

        //  double testMSEBest = 10;

        for (int i = 0; i < numEpochs; i++) {
            if (i % 5 == 0 && i > 0) {
                RegressionEvaluation eval = new RegressionEvaluation();
                System.out.println("Evaluating validation set...");
                while (testIterator.hasNext()) {
                    DataSet set = testIterator.next();
                    INDArray output = model.output(set.getFeatures());
                    eval.eval(set.getLabels(), output, labelsMask);
                }
                testIterator.reset();
                System.out.println(eval.stats());

            /*    if (eval.meanSquaredError(0) < testMSEBest) {
                    ModelLoader.saveModel(model, "D:\\model18best.zip");
                    System.out.println("Saved as best at epoch: " + model.getEpochCount());
                    testMSEBest = eval.meanSquaredError(0);
                }
                System.out.println("Best validation MSE: " + testMSEBest);
                System.out.println("Current validation MSE: " + eval.meanSquaredError(0));
            */

                ModelLoader.saveModel(model, "D:\\model2.zip");
                System.out.println("Saved at epoch: " + model.getEpochCount());
            }
            model.fit(trainIterator);
        }

        ModelLoader.saveModel(model, "D:\\model2.zip");
    }
}

