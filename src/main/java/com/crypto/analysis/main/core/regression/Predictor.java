package com.crypto.analysis.main.core.regression;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.mutils.ModelLoader;
import com.crypto.analysis.main.core.ndata.CSVCoinDataSet;
import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.conf.layers.recurrent.Bidirectional;
import org.deeplearning4j.nn.conf.layers.recurrent.TimeDistributed;
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
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.util.Scanner;

public class Predictor {
    public static void main(String[] args) {
        CudaEnvironment.getInstance().getConfiguration().setMaximumDeviceCacheableLength(1024 * 1024 * 2048L).setMaximumDeviceCache((long) (0.5 * 6096 * 1024 * 1024 * 2048L)).setMaximumHostCacheableLength(1024 * 1024 * 2048L).setMaximumHostCache((long) (0.5 * 6096 * 1024 * 1024 * 2048L));
        Nd4j.getMemoryManager().setAutoGcWindow(100000);
        Nd4j.getEnvironment().allowHelpers(false);

        Scanner sc = new Scanner(System.in);

        System.out.println("Enter count of epochs: ");
        int numEpochs = sc.nextInt();
        sc.nextLine();

        System.out.println("Enter count of epochs to save: ");
        int countToSave = sc.nextInt();
        sc.nextLine();

        System.out.println("Enter data length: ");
        DataLength length = DataLength.valueOf(sc.nextLine());

        System.out.println("Enter time frame: ");
        TimeFrame tf = TimeFrame.valueOf(sc.nextLine());

        System.out.println("Enter path to the model: ");
        String path = sc.nextLine();

        System.out.println("Enter true/false to load the model: ");
        boolean loadModel = Boolean.parseBoolean(sc.nextLine());

        sc.close();

        CSVCoinDataSet setD = new CSVCoinDataSet(Coin.BTCUSDT, tf);
        setD.load();

        RegressionDataSet regressionDataSet = RegressionDataSet.prepareTrainSet(Coin.BTCUSDT, length, setD);
        ListDataSetIterator<DataSet> trainIterator = regressionDataSet.getTrainIterator();
        ListDataSetIterator<DataSet> testIterator = regressionDataSet.getTestIterator();

        INDArray labelsMask = regressionDataSet.getLabelsMask();

        System.out.println(trainIterator.next(1));
        System.out.println();
        System.out.println();
        System.out.println(testIterator.next(1));

        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(123)
                .trainingWorkspaceMode(WorkspaceMode.ENABLED)
                .inferenceWorkspaceMode(WorkspaceMode.ENABLED)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer)
                .weightInit(WeightInit.UNIFORM)
                .activation(Activation.TANH)
                .cacheMode(CacheMode.DEVICE)
                .convolutionMode(ConvolutionMode.Same)
                .l2(2.56e-4)
                .updater(new Adam())
                .list()
                .setInputType(InputType.recurrent(regressionDataSet.getCountInput(), regressionDataSet.getSequenceLength(), RNNFormat.NCW))
                .layer(0, new LSTM.Builder()
                        .nIn(regressionDataSet.getCountInput())
                        .nOut(100)
                        .build())
                .layer(1, new Convolution1DLayer.Builder()
                        .nIn(100)
                        .nOut(100)
                        .kernelSize(6)
                        .stride(1)
                        .dropOut(0.9)
                        .build())
                .layer(2, new Subsampling1DLayer.Builder()
                        .poolingType(PoolingType.MAX)
                        .kernelSize(3)
                        .stride(1)
                        .build())
                .layer(3, new TimeDistributed(
                        new DenseLayer.Builder()
                                .nIn(100)
                                .nOut(100)
                                .activation(Activation.TANH)
                                .build()))
                .layer(4, new DropoutLayer(0.8))
                .layer(5, new Bidirectional(
                        Bidirectional.Mode.CONCAT,
                        new LSTM.Builder()
                                .nOut(100)
                                .build()))
                .layer(6, new DropoutLayer(0.8))
                .layer(7, new Bidirectional(
                        Bidirectional.Mode.CONCAT,
                        new LSTM.Builder()
                                .nOut(100)
                                .build()))
                .layer(8, new DropoutLayer(0.85))
                .layer(9, new Bidirectional(
                        Bidirectional.Mode.CONCAT,
                        new LSTM.Builder()
                                .nOut(100)
                                .build()))
                .layer(10, new DropoutLayer(0.9))
                .layer(11, new TimeDistributed(
                        new DenseLayer.Builder()
                                .nOut(200)
                                .activation(Activation.TANH)
                                .build()))
                .layer(12, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(200)
                        .nOut(regressionDataSet.getCountOutput())
                        .build())
                .backpropType(BackpropType.Standard)
                .build();

        MultiLayerNetwork model;
        if (loadModel) {
            model = ModelLoader.loadNetwork(path);
        } else {
            model = new MultiLayerNetwork(config);
        }

        model.init();
        System.out.println(model.summary());

        System.out.println("Count input params: " + regressionDataSet.getCountInput());
        System.out.println("Count output params: " + regressionDataSet.getCountOutput());
        System.out.println("Count input objects: " + regressionDataSet.getSequenceLength());
        System.out.println("Data length: " + length);
        System.out.println("Time frame: " + tf);
        System.out.println("Number of epochs: " + numEpochs);
        System.out.println("Model path: " + path);

        System.out.println();

        UIServer uiServer = UIServer.getInstance();
        StatsStorage statsStorage = new FileStatsStorage(new File(System.getProperty("java.io.tmpdir"), "ui-stats.dl4j"));

        model.setListeners(new StatsListener(statsStorage, 10), new ScoreIterationListener(10));
        uiServer.attach(statsStorage);

        for (int i = 0; i < numEpochs; i++) {
            if (i % countToSave == 0 && i > 0) {
                RegressionEvaluation eval = new RegressionEvaluation();
                System.out.println("Evaluating validation set...");
                while (testIterator.hasNext()) {
                    DataSet set = testIterator.next();
                    INDArray output = model.output(set.getFeatures(), false, null, set.getLabelsMaskArray());
                    eval.eval(set.getLabels(), output, labelsMask);
                }
                testIterator.reset();
                System.out.println(eval.stats());

                ModelLoader.saveModel(model, path);
                System.out.println("Saved at epoch: " + model.getEpochCount());
            }
            model.fit(trainIterator);
        }

        ModelLoader.saveModel(model, path);
    }
}

