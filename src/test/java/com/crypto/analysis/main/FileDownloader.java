package com.crypto.analysis.main;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FileDownloader {
    public static void main(String[] args) {
        // Входные данные
        double[][][] inputArray = new double[][][]{
                {
                        {5000, 5500, 5200},
                        {12000, 13000, 12500},
                        {0.1, 0.15, 0.15}
                }
        };
        INDArray input = Nd4j.createFromArray(inputArray);

        // Выходные данные
        double[][][] outputArray = new double[][][]{
                {
                        {5300, 12800, 0.12}
                }
        };
        INDArray output = Nd4j.createFromArray(outputArray);

        INDArray featuresMask = Nd4j.ones(1, 3);
        INDArray labelsMask = Nd4j.zeros(1, 3);
        labelsMask.putScalar(new int[]{0, 0}, 1.0);


        // Создание набора данных
        DataSet dataSet = new DataSet(input, output, featuresMask, labelsMask);

        // Создание итератора
        List<DataSet> listDs = new ArrayList<>();
        listDs.add(dataSet);
        ListDataSetIterator<DataSet> iterator = new ListDataSetIterator<>(listDs, 1);
        NormalizerMinMaxScaler normalizer = new NormalizerMinMaxScaler();
        normalizer.fitLabel(true);
        normalizer.fit(iterator);
        iterator.reset();
        iterator.setPreProcessor(normalizer);
        // Конфигурация сети
        MultiLayerNetwork model = new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                .seed(12345)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.01))
                .list()
                .layer(new LSTM.Builder().nIn(3).nOut(3).activation(Activation.TANH).build())
                .layer(new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY).nIn(3).nOut(1).build())
                .build()
        );

        model.init();
        model.setListeners(new ScoreIterationListener(10));

        // Обучение модели
        for (int i = 0; i < 10; i++) {
            model.fit(iterator);
            iterator.reset();
        }

        double[][][] newInput = new double[][][]{
                {
                        {5500, 5500, 5200},
                        {12000, 13000, 12500},
                        {0.1, 0.15, 0.15}
                }
        };
        INDArray predict = model.output(Nd4j.createFromArray(newInput), false, featuresMask, labelsMask);
        normalizer.revertLabels(predict);
        System.out.println(predict);
    }
}