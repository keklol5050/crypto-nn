package com.crypto.analysis.main;

import com.crypto.analysis.main.enumerations.Coin;
import com.crypto.analysis.main.funding.FundingTrainSet;
import com.crypto.analysis.main.funding.FundingTrainSetFactory;
import com.crypto.analysis.main.funding.csv_datasets.CSVRegressionClassificationFundingDataSet;
import com.crypto.analysis.main.funding.csv_datasets.CSVRegressionFundingDataSet;
import com.crypto.analysis.main.funding.csv_datasets.CSVSingleClassificationFundingDataSet;
import com.crypto.analysis.main.model.ModelLoader;
import com.crypto.analysis.main.vo.TrainSetElement;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.adapter.SingletonDataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.Collections;
import java.util.LinkedList;

public class LstmEx {

    public static void main(String[] args) {
        int numEpochs = 300;
        CSVRegressionFundingDataSet regression = new CSVRegressionFundingDataSet(Coin.BTCUSDT, 10);
        regression.load();
        LinkedList<TrainSetElement> elements = regression.getData();
        Collections.shuffle(elements);

        LinkedList<double[][]> regressionTrainData =  new LinkedList<>();
        LinkedList<double[][]> regressionTrainResult = new LinkedList<>();

        LinkedList<double[][]> regressionTestData = new LinkedList<>();
        LinkedList<double[][]> regressionTestResult = new LinkedList<>();

        int count = elements.size();
        int max = count-count/6;

        for (int i = 0; i < count; i++) {
            if (i < max) {
                regressionTrainData.add(elements.get(i).getDataMatrix());
                regressionTrainResult.add(elements.get(i).getResultMatrix());
            } else {
                regressionTestData.add(elements.get(i).getDataMatrix());
                regressionTestResult.add(elements.get(i).getResultMatrix());
            }
        }

        LinkedList<DataSet> regressionSets = new LinkedList<>();

        int sequenceLengthRegression = regressionTrainResult.get(0)[0].length;
        int inputCount = regressionTrainData.get(0).length;
        int outputCount = regressionTrainResult.get(0).length;

        INDArray labelsMask = Nd4j.zeros(outputCount, sequenceLengthRegression);
        labelsMask.putScalar(new int[]{0, 0}, 1.0);

        for (int i = 0; i < regressionTrainData.size(); i++) {
            double[][] inputData = regressionTrainData.get(i);
            double[][] outputData = regressionTrainResult.get(i);

            INDArray input = Nd4j.createFromArray(new double[][][]{inputData});
            INDArray labels = Nd4j.createFromArray(new double[][][]{outputData});

            DataSet set = new DataSet(input, labels, null, labelsMask);
            regressionSets.add(set);
        }
        DataSetIterator iterator = new ListDataSetIterator<>(regressionSets, regressionSets.size());

        NormalizerMinMaxScaler normalizer = new NormalizerMinMaxScaler();
        normalizer.fitLabel(true);

        normalizer.fit(iterator);
        iterator.reset();
        iterator.setPreProcessor(normalizer);

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                .updater(new Adam(0.01))
                .list()
                .layer(new LSTM.Builder().nIn(inputCount).nOut(16)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.TANH)
                        .build())
                .layer(new LSTM.Builder().nIn(16).nOut(16)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.TANH)
                        .build())
                .layer(new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.IDENTITY)
                        .nIn(16).nOut(outputCount)
                        .build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(10));

        for (int i = 0; i < numEpochs; i++) {
            model.fit(iterator);
            iterator.reset();
        }

        int countRight = 0;

        for (int i = 0; i < regressionTestData.size(); i++) {
            INDArray input = Nd4j.createFromArray(new double[][][]{regressionTestData.get(i)});
            normalizer.transform(input);
            INDArray output = model.output(input, false, null, labelsMask);
            normalizer.revertLabels(output);
            double[][] result = regressionTestResult.get(i);
            double indexR = result[0][0];
            double indexP = output.getDouble(0);
            double diff = Math.abs(indexP - indexR);
            double percentDiff =  Math.abs((diff / indexR) * 100); // Разница в процентах

            System.out.printf("real: %s, predicted:%s, percent difference: %.2f%%\n", indexR, indexP, percentDiff);

            if (percentDiff < 5) {
                countRight++;
            }
        }
        System.out.println("Percentage: " + ((double) countRight/(double) regressionTestData.size()) * 100 + '%');

    }
}