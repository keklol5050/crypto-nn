package com.crypto.analysis.main.data_utils;

import com.crypto.analysis.main.vo.CandleObject;
import org.deeplearning4j.datasets.iterator.utilty.SingletonDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
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
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.ArrayList;
import java.util.List;

public class BitcoinPricePrediction {

    public static void main(String[] args) {
        int numInput = 5;

        int numOutput = 1;
        int numEpochs = 10000;

        List<double[]> inputList = new ArrayList<>();
        List<Double> outputList = new ArrayList<>();


        BinanceDataUtil bdu = new BinanceDataUtil("BTCUSDT", "15m", 102);
        List<CandleObject> candles = bdu.getCandles();
        CandleObject res = candles.remove(candles.size()-1);

        for (int i = 0; i < candles.size()-1; i++) {
            double[] input = new double[numInput];
            CandleObject obj = candles.get(i);

            input[0] = obj.getOpen()/10000;
            input[1] = obj.getLow()/10000;
            input[2] = obj.getHigh()/10000;
            input[3] = obj.getClose()/10000;
            input[4] = obj.getVolume()/10000;

            double output = candles.get(i+1).getClose()/10000;

            inputList.add(input);
            outputList.add(output);
        }

        int dataSize = inputList.size();
        INDArray inputArray = Nd4j.create(new double[dataSize][numInput]);
        INDArray outputArray = Nd4j.create(new double[dataSize][1]);

        for (int i = 0; i < dataSize; i++) {
            for (int j = 0; j < numInput; j++) {
                inputArray.putScalar(i, j, inputList.get(i)[j]);
            }
            outputArray.putScalar(i, 0, outputList.get(i));
        }

        DataSet dataSet = new DataSet(inputArray, outputArray);
        DataSetIterator iterator = new SingletonDataSetIterator(dataSet);

        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(0.01))
                .list()
                .layer(0, new DenseLayer.Builder().nIn(numInput).nOut(20)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(20).nOut(15)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new DenseLayer.Builder().nIn(15).nOut(10)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                .layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.IDENTITY)
                        .nIn(10).nOut(numOutput).build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(config);
        model.init();
        model.setListeners(new ScoreIterationListener(10));

        for (int epoch = 0; epoch < numEpochs; epoch++) {
            model.fit(iterator);
        }


        double[] newInputArray = new double[5];
        newInputArray[0] = res.getOpen()/10000;
        newInputArray[1] = res.getLow()/10000;
        newInputArray[2] = res.getHigh()/10000;
        newInputArray[3] = res.getClose()/10000;
        newInputArray[4] = res.getVolume()/10000;
        INDArray newInput = Nd4j.create(new double[][]{newInputArray});
        INDArray predictedOutput = model.output(newInput, false);
        System.out.println("Predicted Bitcoin Price: " + predictedOutput.getDouble(0)*10000);
    }
}