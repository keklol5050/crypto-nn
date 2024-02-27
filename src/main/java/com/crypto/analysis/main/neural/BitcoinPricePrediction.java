package com.crypto.analysis.main.neural;

import com.crypto.analysis.main.data_utils.BinanceDataMultipleInstance;
import com.crypto.analysis.main.enumerations.Periods;
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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BitcoinPricePrediction {

    public static void main(String[] args) throws JsonProcessingException {
        int numInput = 210;

        int numOutput = 1;
        int numEpochs = 10000;
        TrainDataSet trainSet = new TrainDataSet("BTCUSDT");
        trainSet.prepareTrainSet();

        List<double[]> inputList = trainSet.getFinalTrainSet();
        List<Double> outputList = trainSet.getFinalTrainResult();


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
                .updater(new Adam(0.001))
                .list()
                .layer(0, new DenseLayer.Builder().nIn(numInput).nOut(96)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(64).nOut(32)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new DenseLayer.Builder().nIn(32).nOut(16)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                .layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.IDENTITY)
                        .nIn(16).nOut(numOutput).build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(config);
        model.init();
        model.setListeners(new ScoreIterationListener(10));

        for (int epoch = 0; epoch < numEpochs; epoch++) {
            model.fit(iterator);
        }


       while(true) {
           DataObject[] input = BinanceDataMultipleInstance.getLatestInstances("BTCUSDT", Periods.FIFTEEN_MINUTES);
           LinkedList<DataObject> trainData = new LinkedList<>(Arrays.asList(input));
           int index = 0;
           double[] inputDataArray = new double[210];
           for (int j = 0; j < 10; j++) {
               DataObject obj = trainData.removeLast();
               double[] params = obj.getParamArray();
               System.arraycopy(params, 0, inputDataArray, inputDataArray.length - params.length - index, params.length);
               index += params.length;
           }
           INDArray newInput = Nd4j.create(new double[][]{inputDataArray});
           INDArray predictedOutput = model.output(newInput, false);
           System.out.println("Predicted Bitcoin Price: " + predictedOutput.getDouble(0) * 10000);
           try {
               TimeUnit.MINUTES.sleep(5);
           } catch (InterruptedException e) {
               throw new RuntimeException(e);
           }
       }
    }
}