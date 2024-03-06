package com.crypto.analysis.main;

import com.crypto.analysis.main.model.ModelLoader;
import com.crypto.analysis.main.data.train.TrainDataSet;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.adapter.SingletonDataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Arrays;
import java.util.LinkedList;

public class TrainModel {

    public static void main(String[] args)  {
        /*long start = System.currentTimeMillis();

        int numInputs = 320;
        int numOutputs = 3;
        int numEpochs = 10000;

        TrainDataSet trainSet = new TrainDataSet("BTCUSDT");
        trainSet.prepareTrainSet();

        LinkedList<double[][]> in = trainSet.getTrainData();
        LinkedList<double[]> out = trainSet.getTrainResult();

        LinkedList<double[]> inputList = new LinkedList<>();

        for (double[][] inArr : in) {
            inputList.add(Arrays.stream(inArr).flatMapToDouble(Arrays::stream).toArray());
        }

        int dataSize = inputList.size();
        INDArray inputArray = Nd4j.create(new double[dataSize][numInputs]);
        INDArray outputArray = Nd4j.create(new double[dataSize][numOutputs]);

        for (int i = 0; i < dataSize; i++) {
            for (int j = 0; j < numInputs; j++) {
                inputArray.putScalar(i, j, inputList.get(i)[j]);
            }

            double[] outArr = out.get(i);
            for (int j = 0; j < numOutputs; j++) {
                outputArray.putScalar(i, j, outArr[j+1]);
            }
        }

        DataSet dataSet = new DataSet(inputArray, outputArray);

        DataNormalization normalizer = new NormalizerMinMaxScaler();
        normalizer.fitLabel(true);

        DataSetIterator iterator = new SingletonDataSetIterator(dataSet);

        normalizer.fit(iterator);
        iterator.reset();

        iterator.setPreProcessor(normalizer);

        MultiLayerNetwork model = ModelLoader.loadModel("D:\\data.zip" );

        model.init();
        model.setListeners(new ScoreIterationListener(10));

        System.out.println(model.summary());

        for (int i = 0; i < numEpochs; i++) {
            model.fit(iterator);
        }

        ModelLoader.saveModel(model, "D:\\data.zip" );

        LinkedList<double[][]> testSet = trainSet.getTestData();
        LinkedList<double[]> testResult = trainSet.getTestResult();
        int countRight = 0;

        for (int i = 0; i < testSet.size(); i++) {
            INDArray newInput = Nd4j.create(new double[][]{Arrays.stream(testSet.get(i)).flatMapToDouble(Arrays::stream).toArray()});
            normalizer.transform(newInput);
            INDArray predictedOutput = model.output(newInput, false);
            normalizer.revertLabels(predictedOutput);
            double predictionHigh = predictedOutput.getDouble(0);
            double predictionLow = predictedOutput.getDouble(1);
            double realLow = testResult.get(i)[1];
            double realHigh = testResult.get(i)[2];
            boolean isRight = (Math.abs(predictionLow - realLow) < 200) && (Math.abs(predictionHigh - realHigh) < 200);
            if (isRight) countRight++;
            System.out.printf("Predicted low: %f, real: %f, predicted high: %f, real: %f; is right (daily, 200) :%s ",
                    predictionLow, realLow, predictionHigh, realHigh, isRight);
            System.out.println();
        }

        System.out.println("Right: "+countRight + " from " + testSet.size());
        System.out.println("Percentage: " + ((double) countRight/(double) testSet.size()) * 100 + '%');
        System.out.println("Time taken: " + ((System.currentTimeMillis()-start)/1000)/60 + " minutes");

         */
    }
}