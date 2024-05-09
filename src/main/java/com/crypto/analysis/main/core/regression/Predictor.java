package com.crypto.analysis.main.core.regression;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.mutils.ModelLoader;
import com.crypto.analysis.main.core.ndata.CSVCoinDataSet;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.conf.layers.recurrent.Bidirectional;
import org.deeplearning4j.nn.conf.layers.recurrent.TimeDistributed;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.nd4j.jita.conf.CudaEnvironment;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class Predictor {
    public static void main(String[] args) {
        int numEpochs = 1000;

        DataLength length = DataLength.L100_6;
        TimeFrame tf = TimeFrame.ONE_HOUR;

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
                .weightInit(WeightInit.LECUN_NORMAL)
                .activation(Activation.TANH)
                .cacheMode(CacheMode.DEVICE)
                .l2(3.20e-4)
                .updater(new Adam())
                .list()
                .setInputType(InputType.recurrent(regressionDataSet.getCountInput(), regressionDataSet.getSequenceLength(), RNNFormat.NCW))
                .layer(0, new Bidirectional(
                        Bidirectional.Mode.CONCAT,
                        new LSTM.Builder()
                                .nIn(regressionDataSet.getCountInput())
                                .nOut(200)
                                .build()))
                .layer(1, new Bidirectional(
                        Bidirectional.Mode.CONCAT,
                        new LSTM.Builder()
                                .nOut(400)
                                .build()))
                .layer(2, new Bidirectional(
                        Bidirectional.Mode.CONCAT,
                        new LSTM.Builder()
                                .nOut(400)
                                .build()))
                .layer(3, new Bidirectional(
                        Bidirectional.Mode.CONCAT,
                        new LSTM.Builder()
                                .nOut(200)
                                .build()))
                .layer(4, new TimeDistributed(
                        new DenseLayer.Builder()
                                .nOut(200)
                                .activation(Activation.TANH)
                                .build()))
                .layer(5, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(200)
                        .nOut(regressionDataSet.getCountOutput())
                        .build())
                .backpropType(BackpropType.Standard)
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(config);
        model.init();
        CudaEnvironment.getInstance().getConfiguration().setMaximumDeviceCacheableLength(1024 * 1024 * 2048L).setMaximumDeviceCache((long) (0.5 * 6096 * 1024 * 1024 * 2048L)).setMaximumHostCacheableLength(1024 * 1024 * 2048L).setMaximumHostCache((long) (0.5 * 6096 * 1024 * 1024 * 2048L));
        Nd4j.getMemoryManager().setAutoGcWindow(100000);

        System.out.println(model.summary());

        System.out.println("Count input params: " + regressionDataSet.getCountInput());
        System.out.println("Count output params: " + regressionDataSet.getCountOutput());
        System.out.println("Count input objects: " + regressionDataSet.getSequenceLength());

        System.out.println();

        model.setListeners(new ScoreIterationListener(10));

        //  double testMSEBest = 10;

        for (int i = 0; i < numEpochs; i++) {
            if (i % 5 == 0 && i > 0) {
                RegressionEvaluation eval = new RegressionEvaluation();
                System.out.println("Evaluating validation set...");
                while (testIterator.hasNext()) {
                    DataSet set = testIterator.next();
                    INDArray output = model.output(set.getFeatures(), false, null, set.getLabelsMaskArray());
                    eval.eval(set.getLabels(), output, labelsMask);
                }
                testIterator.reset();
                System.out.println(eval.stats());

            /*    if (eval.meanSquaredError(0) < testMSEBest) {
                    ModelLoader.saveModel(regression, "D:\\model18best.zip");
                    System.out.println("Saved as best at epoch: " + regression.getEpochCount());
                    testMSEBest = eval.meanSquaredError(0);
                }
                System.out.println("Best validation MSE: " + testMSEBest);
                System.out.println("Current validation MSE: " + eval.meanSquaredError(0));
            */

                ModelLoader.saveModel(model, "D:\\model14.zip");
                System.out.println("Saved at epoch: " + model.getEpochCount());
            }
            model.fit(trainIterator);
        }

        ModelLoader.saveModel(model, "D:\\model14.zip");
    }
}

