package com.crypto.analysis.main.model;

import com.crypto.analysis.main.data.train.TrainDataSet;
import com.crypto.analysis.main.enumerations.DataLength;
import org.deeplearning4j.datasets.iterator.JointMultiDataSetIterator;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.MultiDataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.MultiNormalizerMinMaxScaler;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Model {
    private final String symbol;
    private final int numEpochs;
    private final String pathToModel;
    private ComputationGraph model;
    private JointMultiDataSetIterator iterator;
    private MultiNormalizerMinMaxScaler normalizer;
    private final List<TrainDataSet> trainList = new ArrayList<>();
    private static final double LEARNING_RATE = 0.01;
    private final int batchSize;
    private final int sequenceLength;

    public Model(String symbol, int numEpochs, int batchSize, int sequenceLength, String pathToModel) {
        this.symbol = symbol;
        this.numEpochs = numEpochs;
        this.batchSize = batchSize;
        this.sequenceLength = sequenceLength;

        this.pathToModel = pathToModel;
        init();
    }

    public Model(String symbol, int numEpochs, int batchSize, int sequenceLength) {
        this.symbol = symbol;
        this.numEpochs = numEpochs;
        this.batchSize = batchSize;
        this.sequenceLength = sequenceLength;

        this.pathToModel = null;
        init();
    }


    public static void main(String[] args) {
        Model model = new Model("BTCUSDT", 10, 1, 5, "D:\\model.zip");
        model.start();
    }

    private void init() {
        if (pathToModel != null) {
            if (Files.exists(Path.of(pathToModel))) {
                model = ModelLoader.loadModel(pathToModel);
            } else {
                model = createModel();
            }
        } else model = createModel();
        assert model != null;
        model.setListeners(new ScoreIterationListener(10));

        iterator = getDataSetIterator();

        normalizer = new MultiNormalizerMinMaxScaler();
        normalizer.fitLabel(true);
        normalizer.fit(iterator);

        iterator.reset();
        iterator.setPreProcessor(normalizer);
    }


    public void start() {
        long start = System.currentTimeMillis();

        System.out.println(model.summary());
        for (int i = 0; i < numEpochs; i++) {
            model.fit(iterator);
        }

        if (pathToModel != null) ModelLoader.saveModel(model, pathToModel);

        testModel();

        System.out.println("Time taken: " + ((System.currentTimeMillis() - start) / 1000) / 60 + " minutes");
    }


    private void testModel() {
        LinkedList<double[][]> d30values = trainList.get(0).getTestData();
        LinkedList<double[][]> d70values = trainList.get(1).getTestData();
        LinkedList<double[][]> d100values = trainList.get(2).getTestData();

        LinkedList<double[][]> d30result = trainList.get(0).getTestResult();
        LinkedList<double[][]> d70result = trainList.get(1).getTestResult();
        LinkedList<double[][]> d100result = trainList.get(2).getTestResult();

        int pred30acc = 0;
        int pred70acc = 0;
        int pred100acc = 0;

        for (int i = 0; i < d30values.size(); i++) {
            INDArray newInput30 = Nd4j.create(new int[]{batchSize, 30, sequenceLength});
            for (int k = 0; k < 30; k++) {
                for (int j = 0; j < sequenceLength; j++) {
                    newInput30.putScalar(0, k, j, d30values.get(i)[k][j]);
                }
            }

            INDArray newInput70 = Nd4j.create(new int[]{batchSize, 70, sequenceLength});
            for (int k = 0; k < 70; k++) {
                for (int j = 0; j < sequenceLength; j++) {
                    newInput70.putScalar(0, k, j, d70values.get(i)[k][j]);
                }
            }

            INDArray newInput100 = Nd4j.create(new int[]{batchSize, 100, sequenceLength});
            for (int k = 0; k < 100; k++) {
                for (int j = 0; j < sequenceLength; j++) {
                    newInput100.putScalar(0, k, j, d100values.get(i)[k][j]);
                }
            }
            INDArray[] newInput = new INDArray[]{newInput30, newInput70, newInput100};
            INDArray[] predictions = model.output(newInput);

            boolean is30True = false;
            boolean is70True = false;
            boolean is100True = false;

            INDArray prediction30 = predictions[0];
            double[][] result30 = d30result.get(i);

            INDArray firstPred30 = prediction30.slice(0).getRow(0);
            double[] firstResult30 = result30[0];

            for (int j = 0; j < firstResult30.length; j++) {
                double da = firstPred30.getDouble(j);
                double ra = firstResult30[j];
                double dlt = Math.abs((da * 100) - (ra * 100));
                if (dlt > 2) break;
                else if (j == firstResult30.length - 1) {
                    pred30acc++;
                    is30True = true;
                }
            }

            INDArray prediction70 = predictions[1];
            double[][] result70 = d70result.get(i);

            INDArray firstPred70 = prediction70.slice(0).getRow(0);
            double[] firstResult70 = result70[0];
            for (int j = 0; j < firstResult70.length; j++) {
                double da = firstPred70.getDouble(j);
                double ra = firstResult70[j];
                double dlt = Math.abs((da * 100) - (ra * 100));
                if (dlt > 2) break;
                else if (j == firstResult70.length - 1) {
                    pred70acc++;
                    is70True = true;
                }
            }

            INDArray prediction100 = predictions[2];
            double[][] result100 = d100result.get(i);

            INDArray firstPred100 = prediction100.slice(0).getRow(0);
            double[] firstResult100 = result100[0];
            for (int j = 0; j < firstResult100.length; j++) {
                double da = firstPred100.getDouble(j);
                double ra = firstResult100[j];
                double dlt = Math.abs((da * 100) - (ra * 100));
                if (dlt > 2) break;
                else if (j == firstResult100.length - 1) {
                    pred100acc++;
                    is100True = true;
                }
            }

            System.out.printf("30 candles: %s, 70 candles: %s, 100 candles: %s\n", is30True, is70True, is100True);
        }
        System.out.printf("Total result for candles: 30 candles: %d, 70 candles: %d, 100 candles: %d\n", pred30acc, pred70acc, pred100acc);
        System.out.println("30 candles percentage: " + ((double) pred30acc / (double) d30values.size()) * 100 + '%');
        System.out.println("70 candles percentage: " + ((double) pred70acc / (double) d70values.size()) * 100 + '%');
        System.out.println("100 candles percentage: " + ((double) pred100acc / (double) d100values.size()) * 100 + '%');
        System.out.println("Total percentage: " + ((double) ((pred30acc + pred70acc + pred100acc) / 3) / (double) d100values.size()) * 100 + '%');
    }


    private JointMultiDataSetIterator getDataSetIterator() {
        List<DataSetIterator> iterators = new ArrayList<DataSetIterator>();

        for (DataLength dl : DataLength.values()) {
            TrainDataSet trainSet = TrainDataSet.prepareTrainSet(symbol, dl);
            int countInput = dl.getCountInput();
            int countOutput = dl.getCountOutput();

            LinkedList<double[][]> inputList = trainSet.getTrainData();
            LinkedList<double[][]> outputList = trainSet.getTrainResult();

            LinkedList<DataSet> sets = new LinkedList<>();

            for (int i = 0; i < inputList.size(); i++) {
                double[][] inputData = inputList.get(i);
                double[][] outputData = outputList.get(i);
                INDArray input = Nd4j.create(new int[]{batchSize, countInput, sequenceLength});
                for (int k = 0; k < countInput; k++) {
                    for (int j = 0; j < sequenceLength; j++) {
                        input.putScalar(0, k, j, inputData[k][j]);
                    }
                }
                INDArray labels = Nd4j.create(new int[]{batchSize, countOutput, sequenceLength});
                for (int k = 0; k < countOutput; k++) {
                    for (int j = 0; j < sequenceLength; j++) {
                        labels.putScalar(0, k, j, outputData[k][j]);
                    }
                }
                DataSet set = new DataSet(input, labels);
                sets.add(set);
            }

            iterators.add(new ListDataSetIterator<>(sets, sets.size()));
            trainList.add(trainSet);
        }

        return new JointMultiDataSetIterator(iterators.toArray(new DataSetIterator[0]));
    }


    private ComputationGraph createModel() {
        ComputationGraph model = new ComputationGraph(getConfiguration());
        model.init();
        return model;
    }


    private ComputationGraphConfiguration getConfiguration() {
        return new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                .weightInit(WeightInit.XAVIER)
                .activation(Activation.TANH)
                .updater(new Adam(LEARNING_RATE))
                .graphBuilder()
                .addInputs("input_30", "input_70", "input_100")

                // Layer 1
                .addLayer("lstm1_30", new LSTM.Builder()
                        .nIn(30)
                        .nOut(384)
                        .build(), "input_30")
                .addLayer("lstm2_30", new LSTM.Builder()
                        .nIn(384)
                        .nOut(384)
                        .build(), "lstm1_30")
                .addLayer("lstm3_30", new LSTM.Builder()
                        .nIn(384)
                        .nOut(128)
                        .build(), "lstm2_30")
                .addLayer("lstm4_30", new LSTM.Builder()
                        .nIn(128)
                        .nOut(64)
                        .build(), "lstm3_30")
                .addLayer("lstm5_30", new LSTM.Builder()
                        .nIn(64)
                        .nOut(32)
                        .build(), "lstm4_30")
                .addLayer("lstm6_30", new LSTM.Builder()
                        .nIn(32)
                        .nOut(5)
                        .build(), "lstm5_30")
                .addLayer("output_30", new RnnOutputLayer.Builder()
                        .nIn(5)
                        .nOut(5)
                        .activation(Activation.IDENTITY)
                        .lossFunction(LossFunctions.LossFunction.MSE)
                        .build(), "lstm6_30")

                // Layer 2
                .addLayer("lstm1_70", new LSTM.Builder()
                        .nIn(70)
                        .nOut(512)
                        .build(), "input_70")
                .addLayer("lstm2_70", new LSTM.Builder()
                        .nIn(512)
                        .nOut(512)
                        .build(), "lstm1_70")
                .addLayer("lstm3_70", new LSTM.Builder()
                        .nIn(512)
                        .nOut(384)
                        .build(), "lstm2_70")
                .addLayer("lstm4_70", new LSTM.Builder()
                        .nIn(384)
                        .nOut(128)
                        .build(), "lstm3_70")
                .addLayer("lstm5_70", new LSTM.Builder()
                        .nIn(128)
                        .nOut(64)
                        .build(), "lstm4_70")
                .addLayer("lstm6_70", new LSTM.Builder()
                        .nIn(64)
                        .nOut(10)
                        .build(), "lstm5_70")
                .addLayer("output_70", new RnnOutputLayer.Builder()
                        .nIn(10)
                        .nOut(10)
                        .activation(Activation.IDENTITY)
                        .lossFunction(LossFunctions.LossFunction.MSE)
                        .build(), "lstm6_70")

                // Layer 3
                .addLayer("lstm1_100", new LSTM.Builder()
                        .nIn(100)
                        .nOut(768)
                        .build(), "input_100")
                .addLayer("lstm2_100", new LSTM.Builder()
                        .nIn(768)
                        .nOut(768)
                        .build(), "lstm1_100")
                .addLayer("lstm3_100", new LSTM.Builder()
                        .nIn(768)
                        .nOut(384)
                        .build(), "lstm2_100")
                .addLayer("lstm4_100", new LSTM.Builder()
                        .nIn(384)
                        .nOut(128)
                        .build(), "lstm3_100")
                .addLayer("lstm5_100", new LSTM.Builder()
                        .nIn(128)
                        .nOut(64)
                        .build(), "lstm4_100")
                .addLayer("lstm6_100", new LSTM.Builder()
                        .nIn(64)
                        .nOut(15)
                        .build(), "lstm5_100")
                .addLayer("output_100", new RnnOutputLayer.Builder()
                        .nIn(15)
                        .nOut(15)
                        .activation(Activation.IDENTITY)
                        .lossFunction(LossFunctions.LossFunction.MSE)
                        .build(), "lstm6_100")

                .setOutputs("output_30", "output_70", "output_100")
                .build();
    }
}
