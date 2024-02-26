package com.crypto.analysis.main;

import com.crypto.analysis.main.data_utils.BinanceDataUtil;
import com.crypto.analysis.main.data_utils.TrainData;
import com.crypto.analysis.main.vo.CandleObject;
import com.crypto.analysis.main.vo.DataObject;
import com.fasterxml.jackson.core.JsonProcessingException;
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

    public static void main(String[] args) throws JsonProcessingException {
        int numInput = 12;

        int numOutput = 1;
        int numEpochs = 10000;

        List<double[]> inputList = new ArrayList<>();
        TrainData train = new TrainData("BTCUSDT", "5m");
        for (DataObject dO : train.getTrainData())
            inputList.add(dO.getParamArray());

        List<Double> outputList = train.getTrainResult();


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


        CandleObject obj = new BinanceDataUtil("BTCUSDT", "5m", 1).getCandles().get(0);

        INDArray newInput = Nd4j.create(obj.getValuesArr());
        INDArray predictedOutput = model.output(newInput, false);
        System.out.println("Predicted Bitcoin Price: " + predictedOutput.getDouble(0));
    }
}