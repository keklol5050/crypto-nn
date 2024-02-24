package com.crypto.analysis.main.data_utils;

import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.eval.RegressionEvaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.GradientNormalization;
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
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.ArrayList;
import java.util.List;

public class SimpleNeuralNetwork {

        // Настройка конфигурации нейросети
        public static void main(String[] args) {
            int seed = 123;
            double learningRate = 0.01;
            int batchSize = 50;
            int nEpochs = 30;

            int numInputs = 100;
            int numOutputs = 1;
            int numHiddenNodes = 20;

            final MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                    .seed(seed)
                    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                    .updater(new Nesterovs(learningRate, 0.9))
                    .list()
                    .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
                            .weightInit(WeightInit.XAVIER)
                            .activation(Activation.RELU)
                            .build())
                    .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                            .weightInit(WeightInit.XAVIER)
                            .activation(Activation.IDENTITY)
                            .nIn(numHiddenNodes).nOut(numOutputs).build())
                    .build();

            MultiLayerNetwork model = new MultiLayerNetwork(conf);
            model.init();

            // Создание данных для обучения
            double[] inputTrain = new double[100];
            for (int i = 0; i < 100; i++) {
                inputTrain[i] = 40000 + i * 50;
            }

            double[][] inputTrainArr = new double[1][];
            inputTrainArr[0] = inputTrain;

            double[][] outputTrainArr = new double[1][];
            outputTrainArr[0] = new double[]{42000};

            DataSet trainSet = new DataSet(Nd4j.create(inputTrainArr), Nd4j.create(outputTrainArr));

            List<DataSet> trainDataList = new ArrayList<>();
            trainDataList.add(trainSet);
            ListDataSetIterator<DataSet> trainData = new ListDataSetIterator<>(trainDataList, batchSize);

            // Обучение модели
            for (int i = 0; i < nEpochs; i++) {
                model.fit(trainData);
            }

            // Создание тестовых данных
            double[] inputTest = new double[100];
            for (int i = 0; i < 100; i++) {
                inputTest[i] = 50000 + i * 50;
            }

            double[][] inputTestArr = new double[1][];
            inputTestArr[0] = inputTest;

            double[][] outputTestArr = new double[1][];
            outputTestArr[0] = new double[]{52000};

            DataSet testSet = new DataSet(Nd4j.create(inputTestArr), Nd4j.create(outputTestArr));

            List<DataSet> testDataList = new ArrayList<>();
            testDataList.add(testSet);
            ListDataSetIterator<DataSet> testData = new ListDataSetIterator<>(testDataList, batchSize);

            // Тестирование модели
            RegressionEvaluation eval = model.evaluateRegression(testData);
            System.out.println(eval.stats());
        }
}
