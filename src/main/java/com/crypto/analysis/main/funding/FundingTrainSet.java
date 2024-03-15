package com.crypto.analysis.main.funding;

import com.crypto.analysis.main.enumerations.Coin;
import lombok.Getter;
import org.deeplearning4j.datasets.iterator.JointMultiDataSetIterator;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.adapter.SingletonDataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.MultiNormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;

import java.util.LinkedList;
@Getter
public class FundingTrainSet {
    private final Coin coin;

    private final LinkedList<double[]> singleClassificationTrainData;
    private final LinkedList<double[]> singleClassificationTrainResult;

    @Getter
    private final LinkedList<double[][]> multiClassificationTrainData;
    @Getter
    private final LinkedList<double[][]> multiClassificationTrainResult;

    private final LinkedList<double[][]> regressionTrainData;
    private final LinkedList<double[][]> regressionTrainResult;

    private final LinkedList<double[]> singleClassificationTestData;
    private final LinkedList<double[]> singleClassificationTestResult;

    private final LinkedList<double[][]> multiClassificationTestData;
    private final LinkedList<double[][]> multiClassificationTestResult;

    private final LinkedList<double[][]> regressionTestData;
    private final LinkedList<double[][]> regressionTestResult;

    private final int batchSize;

    @Getter
    private JointMultiDataSetIterator iterator;
    @Getter
    private MultiNormalizerMinMaxScaler normalizer;

    @Getter
    private int capacity;
    @Getter
    private int sequenceLengthRegression;
    @Getter
    private int sequenceLengthMultiClassification;
    @Getter
    private int numInputsSingleClassification;
    @Getter
    private int numOutputsClassification;


    FundingTrainSet(Coin coin,
                    LinkedList<double[]> singleClassificationTrainData,
                    LinkedList<double[]> singleClassificationTrainResult,
                    LinkedList<double[][]> multiClassificationTrainData,
                    LinkedList<double[][]> multiClassificationTrainResult,
                    LinkedList<double[][]> regressionTrainData,
                    LinkedList<double[][]> regressionTrainResult,
                    LinkedList<double[]> singleClassificationTestData,
                    LinkedList<double[]> singleClassificationTestResult,
                    LinkedList<double[][]> multiClassificationTestData,
                    LinkedList<double[][]> multiClassificationTestResult,
                    LinkedList<double[][]> regressionTestData,
                    LinkedList<double[][]> regressionTestResult,
                    int batchSize, int capacity) {
        this.coin = coin;
        this.singleClassificationTrainData = singleClassificationTrainData;
        this.singleClassificationTrainResult = singleClassificationTrainResult;
        this.multiClassificationTrainData = multiClassificationTrainData;
        this.multiClassificationTrainResult = multiClassificationTrainResult;
        this.regressionTrainData = regressionTrainData;
        this.regressionTrainResult = regressionTrainResult;
        this.singleClassificationTestData = singleClassificationTestData;
        this.singleClassificationTestResult = singleClassificationTestResult;
        this.multiClassificationTestData = multiClassificationTestData;
        this.multiClassificationTestResult = multiClassificationTestResult;
        this.regressionTestData = regressionTestData;
        this.regressionTestResult = regressionTestResult;

        this.batchSize = batchSize;
        this.capacity = capacity;

        init();
    }

    private void init() {
        // single classification
        int dataSize = singleClassificationTrainData.size();
        numInputsSingleClassification = singleClassificationTrainData.get(0).length;
        numOutputsClassification = FundingClassification.values().length;

        INDArray singleClassificationInputArray = Nd4j.create(new double[dataSize][numInputsSingleClassification]);
        INDArray singleClassificationOutputArray = Nd4j.create(new double[dataSize][numOutputsClassification]);

        for (int i = 0; i < dataSize; i++) {
            for (int j = 0; j < numInputsSingleClassification; j++) {
                singleClassificationInputArray.putScalar(i, j, singleClassificationTrainData.get(i)[j]);
            }
            for (int j = 0; j < numOutputsClassification; j++) {
                singleClassificationOutputArray.putScalar(i, j, singleClassificationTrainResult.get(i)[j]);
            };
        }
        DataSet singleClassificationDataSet = new DataSet(singleClassificationInputArray, singleClassificationOutputArray);
        DataSetIterator singleClassificationIterator = new SingletonDataSetIterator(singleClassificationDataSet);

        // multi classification
        LinkedList<DataSet> multipleSets = new LinkedList<>();
        int countInput = multiClassificationTrainData.get(0).length;
        int countOutput = multiClassificationTrainResult.get(0).length;
        sequenceLengthMultiClassification = multiClassificationTrainData.get(0)[0].length;

        for (int i = 0; i < multiClassificationTrainData.size(); i++) {
            double[][] inputData = multiClassificationTrainData.get(i);
            double[][] outputData = multiClassificationTrainResult.get(i);
            INDArray input = Nd4j.create(new int[]{batchSize, countInput, sequenceLengthMultiClassification});
            for (int k = 0; k < countInput; k++) {
                for (int j = 0; j < sequenceLengthMultiClassification; j++) {
                    input.putScalar(0, k, j, inputData[k][j]);
                }
            }
            INDArray labels = Nd4j.create(new int[]{batchSize, countOutput, sequenceLengthMultiClassification});
            for (int k = 0; k < countOutput; k++) {
                for (int j = 0; j < sequenceLengthMultiClassification; j++) {
                    labels.putScalar(0, k, j, outputData[k][j]);
                }
            }

            INDArray featuresMask = Nd4j.ones(1, sequenceLengthMultiClassification);

            INDArray labelsMask = Nd4j.zeros(1, sequenceLengthMultiClassification);
            labelsMask.putScalar(new int[]{0, 0}, 1.0);

            DataSet set = new DataSet(input, labels, featuresMask, labelsMask);
            multipleSets.add(set);
        }
        DataSetIterator multiClassificationIterator = new ListDataSetIterator<>(multipleSets, multipleSets.size());

        // regression
        LinkedList<DataSet> regressionSets = new LinkedList<>();
        int countInputRegression = regressionTrainData.get(0).length;
        int countOutputRegression = regressionTrainResult.get(0).length;
        sequenceLengthRegression = regressionTrainData.get(0)[0].length;

        for (int i = 0; i < regressionTrainData.size(); i++) {
            double[][] inputData = regressionTrainData.get(i);
            double[][] outputData = regressionTrainResult.get(i);
            INDArray input = Nd4j.create(new int[]{batchSize, countInputRegression, sequenceLengthRegression});
            for (int k = 0; k < countInputRegression; k++) {
                for (int j = 0; j < sequenceLengthRegression; j++) {
                    input.putScalar(0, k, j, inputData[k][j]);
                }
            }
            INDArray labels = Nd4j.create(new int[]{batchSize, countOutputRegression, sequenceLengthRegression});
            for (int k = 0; k < countOutputRegression; k++) {
                for (int j = 0; j < sequenceLengthRegression; j++) {
                    labels.putScalar(0, k, j, outputData[k][j]);
                }
            }

            INDArray featuresMask = Nd4j.ones(1, sequenceLengthRegression);

            INDArray labelsMask = Nd4j.zeros(1, sequenceLengthRegression);
            labelsMask.putScalar(new int[]{0, 0}, 1.0);

            DataSet set = new DataSet(input, labels, featuresMask, labelsMask);
            regressionSets.add(set);
        }
        DataSetIterator regressionIterator = new ListDataSetIterator<>(regressionSets, regressionSets.size());

        iterator = new JointMultiDataSetIterator(
                new DataSetIterator[]{singleClassificationIterator, multiClassificationIterator, regressionIterator});

        normalizer = new MultiNormalizerMinMaxScaler();
        normalizer.fitLabel(true);

        normalizer.fit(iterator);
        iterator.reset();
        iterator.setPreProcessor(normalizer);
    }


}
