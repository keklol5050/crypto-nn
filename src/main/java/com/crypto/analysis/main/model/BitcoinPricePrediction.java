package com.crypto.analysis.main.model;

import com.crypto.analysis.main.data.train.TrainDataSet;
import com.crypto.analysis.main.data_utils.normalizers.BatchNormalizer;
import com.crypto.analysis.main.enumerations.Coin;
import com.crypto.analysis.main.enumerations.DataLength;
import com.crypto.analysis.main.enumerations.TimeFrame;
import com.crypto.analysis.main.ndata.CSVCoinDataSet;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DropoutLayer;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.*;

public class BitcoinPricePrediction {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        CSVCoinDataSet setD = new CSVCoinDataSet(Coin.BTCUSDT, TimeFrame.FIFTEEN_MINUTES);
        setD.load();
        TrainDataSet trainSet = TrainDataSet.prepareTrainSet(Coin.BTCUSDT, DataLength.S30_3, setD);

        LinkedList<double[][]> inputList = trainSet.getTrainData();
        LinkedList<double[][]> outputList = trainSet.getTrainResult();

        int numInputs = inputList.get(0).length;
        int numOutputs = outputList.get(0).length;
        int sequenceLength = inputList.get(0)[0].length;
        int numEpochs = 40;

        INDArray labelsMask = Nd4j.zeros(1, sequenceLength);
        labelsMask.putScalar(new int[]{0, 0}, 1.0);

        List<DataSet> sets = new ArrayList<>();
        for (int i = 0; i < inputList.size(); i++) {
            double[][] inputData = inputList.get(i);
            double[][] outputData = outputList.get(i);

            INDArray input = Nd4j.createFromArray(new double[][][]{inputData});
            INDArray labels = Nd4j.createFromArray(new double[][][]{outputData});

            DataSet set = new DataSet(input, labels, null, labelsMask);
            sets.add(set);
        }

        Collections.shuffle(sets);
        DataSetIterator iterator = new ListDataSetIterator<>(sets, 4096);
        BatchNormalizer normalizer = trainSet.getNormalizer();
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                .weightInit(WeightInit.XAVIER)
                .activation(Activation.LEAKYRELU)
                .updater(new Adam(0.01))
                .list()
                .layer(0, new LSTM.Builder().nIn(numInputs).nOut(64).build())
                .layer(1, new LSTM.Builder().nIn(64).nOut(256).build())
                .layer(2, new LSTM.Builder().nIn(256).nOut(256).build())
                .layer(3, new DropoutLayer.Builder().nIn(256).nOut(256)
                        .dropOut(0.2).build())
                .layer(4, new LSTM.Builder().nIn(256).nOut(64).build())
                .layer(5, new LSTM.Builder().nIn(64).nOut(32).build())
                .layer(6, new LSTM.Builder().nIn(32).nOut(16).build())
                .layer(7, new LSTM.Builder().nIn(16).nOut(8).build())
                .layer(8, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(8).nOut(numOutputs)
                        .build())
                .build();

        MultiLayerNetwork model = ModelLoader.loadNetwork("D:\\model1.zip");
        assert model != null;
        model.init();

        System.out.println(model.summary());

        model.setListeners(new ScoreIterationListener(10));

        for (int i = 0; i < numEpochs; i++) {
            if (i%20==0 && i > 0)
                ModelLoader.saveModel(model, "D:\\model1.zip");
            model.fit(iterator);
        }

        ModelLoader.saveModel(model, "D:\\model1.zip");

        LinkedList<double[][]> testSet = trainSet.getTestData();
        LinkedList<double[][]> testResult = trainSet.getTestResult();

        int countRight = 0;
        for (int i = 0; i < testSet.size(); i++) {
            INDArray newInput = Nd4j.createFromArray(new double[][][]{testSet.get(i)});
            INDArray predictedOutput = model.output(newInput, false, null, labelsMask);

      /*      double[][] predicted = {
                    {predictedOutput.slice(0).getRow(0).getDouble(0)},
                    {predictedOutput.slice(0).getRow(1).getDouble(0)}
            };

            double[][] realFin = {{real[0][0]}, {real[1][0]}};

            int rows = predicted.length;
            int cols = predicted[0].length;
            double totalPercentageDifference = 0.0;

            for (int k = 0; k < rows; k++) {
                for (int j = 0; j < cols; j++) {
                    double realValue = realFin[k][j];
                    double predictedValue = predicted[k][j];
                    double percentageDifference = Math.abs(predictedValue - realValue);
                    totalPercentageDifference += percentageDifference;
                }
            }

            double averagePercentageDifference = totalPercentageDifference / (rows * cols);
            if (averagePercentageDifference < 150) countRight++;
               */
            double[][] real = testResult.get(i);
            normalizer.revertLabelsVertical(testSet.get(i), real);

            double[][] predMatrix = predictedOutput.slice(0).toDoubleMatrix();
            normalizer.revertLabelsVertical(testSet.get(i), predMatrix);

            double predicted = predMatrix[0][0];
            double realFin = real[0][0];

            double totalDifference = Math.abs(predicted - realFin);
            if (totalDifference < 150) countRight++;

            System.out.println("Predicted: ");
            System.out.println(predicted);
            System.out.println("Real: ");
            System.out.println(realFin);
            System.out.println("Total difference " + totalDifference);
            System.out.println();
        }
        System.out.println("Percentage: " + ((double) countRight/(double) testSet.size()) * 100 + '%');
        System.out.println("Time taken: " + ((System.currentTimeMillis() - start) / 1000) / 60 + " minutes");
    }
}