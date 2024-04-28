package com.crypto.analysis.main.core.volatility;

import com.crypto.analysis.main.core.classification.ClassificationTrainSet;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.model.DataVisualisation;
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
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;

public class Model {
    public static void main(String[] args) {
        CSVCoinDataSet cs = new CSVCoinDataSet(Coin.BTCUSDT, TimeFrame.FOUR_HOUR);
        cs.load();

        int numEpochs = 600;
        VolatilityTrainSet trainDataSet = VolatilityTrainSet.prepareTrainSet(Coin.BTCUSDT, cs);

        ListDataSetIterator<DataSet> trainIterator = trainDataSet.getTrainIterator();
        ListDataSetIterator<DataSet> testIterator = trainDataSet.getTestIterator();

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
                .l1(3.2e-5)
                .l2(1.28e-4)
                .updater(new Adam())
                .list()
                .layer(0, new LSTM.Builder()
                        .nIn(trainDataSet.getCountInput())
                        .nOut(256)
                        .build())
                .layer(1, new LSTM.Builder()
                        .nIn(256)
                        .nOut(256)
                        .build())
                .layer(2, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(256)
                        .nOut(trainDataSet.getCountOutput())
                        .build())
                .backpropType(BackpropType.Standard)
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(config);
        model.init();

        System.out.println(model.summary());

        UIServer uiServer = UIServer.getInstance();
        StatsStorage statsStorage = new FileStatsStorage(new File(System.getProperty("java.io.tmpdir"), "ui-stats.dl4j"));

        model.setListeners(new StatsListener(statsStorage, 10), new ScoreIterationListener(10));

        uiServer.attach(statsStorage);
        System.gc();
        for (int i = 0; i < numEpochs; i++) {
            if (i % 20 == 0 && i > 0) {
                RegressionEvaluation evaluation = new RegressionEvaluation();
                System.out.println("Evaluating validation set...");
                while (testIterator.hasNext()) {
                    DataSet set = testIterator.next();
                    INDArray output = model.output(set.getFeatures());
                    evaluation.eval(set.getLabels(), output, trainDataSet.getLabelsMask());
                }
                ModelLoader.saveModel(model, "D:\\model1vol.zip");
                System.out.println("Saved at epoch: " + model.getEpochCount());
            }
            model.fit(trainIterator);
            System.gc();
        }

        ModelLoader.saveModel(model, "D:\\model1vol.zip");

        while (testIterator.hasNext()) {
            DataSet set = testIterator.next(1);
            INDArray futures = set.getFeatures();
            INDArray labels = set.getLabels();

            INDArray predictedOutput = model.output(futures, false, null, trainDataSet.getLabelsMask());

            double[][] features = futures.slice(0).toDoubleMatrix();

            double[][] real = labels.slice(0).toDoubleMatrix();

            double[][] predMatrix = predictedOutput.slice(0).toDoubleMatrix();

            double[][] predicted = new double[trainDataSet.getCountOutput()][DataLength.VOLATILITY_REGRESSION.getCountOutput()];
            for (int j = 0; j < trainDataSet.getCountOutput(); j++) {
                System.arraycopy(predMatrix[j], 0, predicted[j], 0, DataLength.VOLATILITY_REGRESSION.getCountOutput());
            }

            double[][] realFin = new double[trainDataSet.getCountOutput()][DataLength.VOLATILITY_REGRESSION.getCountOutput()];
            for (int j = 0; j < trainDataSet.getCountOutput(); j++) {
                System.arraycopy(real[j], 0, realFin[j], 0, DataLength.VOLATILITY_REGRESSION.getCountOutput());
            }
            DataVisualisation.visualizeData("Prediction", "candle length", "price", features[1], realFin[1], predicted[1]);
        }
    }
}
