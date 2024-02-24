package com.crypto.analysis.main.data_utils;

import com.crypto.analysis.main.vo.CandleObject;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Test {
    static int numEpochs = 100; // Пример: 50 эпох обучения
    static int batchSize = 32; // Пример: размер пакета 32

    public static MultiLayerNetwork buildModel(int inputSize, int outputSize) {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.001))
                .list()
                .layer(new GravesLSTM.Builder().nIn(inputSize).nOut(50)
                        .activation(Activation.TANH).build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(50).nOut(outputSize).build())
                .build();

        return new MultiLayerNetwork(conf);
    }
    public static void trainModel(MultiLayerNetwork model, List<CandleObject> trainingData) {
        int inputSize = 11;  // Number of features in CandleObject
        int outputSize = 1;  // Predicting closing price

        model.init();
        model.setListeners(new ScoreIterationListener(10));

        List<DataSet> dataSets = new ArrayList<>();
        for (CandleObject candle : trainingData) {
            INDArray input = Nd4j.create(new double[]{
                    candle.getOpenTime().getTime(),
                    candle.getOpen(),
                    candle.getHigh(),
                    candle.getLow(),
                    candle.getClose(),
                    candle.getVolume(),
                    candle.getCloseTime().getTime(),
                    candle.getQuoteAssetVolume(),
                    candle.getNumberOfTrades(),
                    candle.getTakerBuyBaseAssetVolume(),
                    candle.getTakerBuyQuoteAssetVolume()
            }, new int[]{1, inputSize, 1}); // 1 временной шаг, 11 признаков, 1 временной ряд

            INDArray output = Nd4j.create(new double[]{candle.getClose()}, new int[]{1, outputSize, 1}); // 1 временной шаг, 1 выход, 1 временной ряд

            dataSets.add(new DataSet(input, output));
        }

        DataSetIterator iterator = new ListDataSetIterator<>(dataSets, batchSize);

        for (int i = 0; i < numEpochs; i++) {
            model.fit(iterator);
        }
    }

    public static INDArray predictNextClose(MultiLayerNetwork model, CandleObject currentCandle) {
        int inputSize = 11;  // Number of features in CandleObject
        int outputSize = 1;  // Predicting closing price

        INDArray input = Nd4j.create(new double[]{
                currentCandle.getOpenTime().getTime(),
                currentCandle.getOpen(),
                currentCandle.getHigh(),
                currentCandle.getLow(),
                currentCandle.getClose(),
                currentCandle.getVolume(),
                currentCandle.getCloseTime().getTime(),
                currentCandle.getQuoteAssetVolume(),
                currentCandle.getNumberOfTrades(),
                currentCandle.getTakerBuyBaseAssetVolume(),
                currentCandle.getTakerBuyQuoteAssetVolume()
        }, new int[]{1, inputSize, 1}); // 1 временной шаг, 11 признаков, 1 временной ряд

        return model.rnnTimeStep(input);
    }

    public static void main(String[] args) {
        List<CandleObject> trainingData = fetchTrainingData();  // Implement method to fetch your training data
        int inputSize = 11;  // Number of features in CandleObject
        int outputSize = 1;  // Predicting closing price

        MultiLayerNetwork model = buildModel(inputSize, outputSize);
        trainModel(model, trainingData);

        CandleObject currentCandle = fetchCurrentCandle();  // Implement method to fetch current candle data
        INDArray predictedClose = predictNextClose(model, currentCandle);

        System.out.println("Predicted Closing Price: " + predictedClose);
    }

    public static List<CandleObject> fetchTrainingData() {
        // Здесь вы должны получить реальные данные с биржи или из вашей базы данных
        // В данном примере используем мок-данные

        BinanceDataUtil dataUtil  = new BinanceDataUtil("BTCUSDT", "15m", 1000);
        List<CandleObject> trainingData = dataUtil.getCandles();

        return trainingData;
    }

    public static CandleObject fetchCurrentCandle() {
        // Здесь вы должны получить реальные данные с биржи или из вашей базы данных
        // В данном примере используем мок-данные для текущей свечи
        return new CandleObject(new Date(), 51536, 51653, 51509, 51609, 1040, new Date(), 5.3, 19500, 578.5, 2.98);
    }
}