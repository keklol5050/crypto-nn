package com.crypto.analysis.main.model;

import com.crypto.analysis.main.data_utils.select.StaticData;
import com.crypto.analysis.main.data_utils.select.coin.DataLength;
import lombok.Setter;
import org.deeplearning4j.datasets.iterator.JointMultiDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.RNNFormat;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Setter
public class Model {
    private final String path;

    private int numInputs = StaticData.MODEL_NUM_INPUTS;
    private int numOutputs = StaticData.MODEL_NUM_OUTPUTS;

    private String pathToModel;
    private String pathToAccessor;

    private ComputationGraph model;
    private RelativeAccessor accessor;
    private static final double LEARNING_RATE = StaticData.MODEL_LEARNING_RATE;

    public Model(String path) {
        this.path = path;
    }

    public void init() {
        pathToModel = path + "model.zip";
        pathToAccessor = path + "accessor.zip";

        if (Files.exists(Path.of(pathToModel)) && Files.exists(Path.of(pathToAccessor))) {
            model = ModelLoader.loadGraph(pathToModel);
            accessor = RelativeAccessor.loadAccessor(pathToAccessor);
        } else {
            model = createModel();
        }
    }

    private ComputationGraph createModel() {
        ComputationGraph model = new ComputationGraph(getConfiguration());
        model.init();

        model.setListeners(new ScoreIterationListener(10));
        System.out.println(model.summary());

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
                .setInputTypes(InputType.recurrent(numInputs, DataLength.S50_3.getCountInput(), RNNFormat.NCW),
                        InputType.recurrent(numInputs,  DataLength.L70_6.getCountInput(), RNNFormat.NCW),
                        InputType.recurrent(numInputs,  DataLength.X100_9.getCountInput(), RNNFormat.NCW))
                .addInputs("input_50", "input_70", "input_100")
                .setOutputs("output_50", "output_70", "output_100")

                .addLayer("lstm1_50", new LSTM.Builder()
                        .nIn(numInputs)
                        .nOut(256)
                        .build(), "input_50")
                .addLayer("lstm2_50", new LSTM.Builder()
                        .nIn(256)
                        .nOut(256)
                        .dropOut(0.6)
                        .build(), "lstm1_50")
                .addLayer("lstm3_50", new LSTM.Builder()
                        .nIn(256)
                        .nOut(256)
                        .build(), "lstm2_50")
                .addLayer("lstm4_50", new LSTM.Builder()
                        .nIn(256)
                        .nOut(128)
                        .dropOut(0.8)
                        .build(), "lstm3_50")
                .addLayer("lstm5_50", new LSTM.Builder()
                        .nIn(128)
                        .nOut(64)
                        .build(), "lstm4_50")
                .addLayer("output_50", new RnnOutputLayer.Builder()
                        .nIn(64)
                        .nOut(numOutputs)
                        .activation(Activation.IDENTITY)
                        .lossFunction(LossFunctions.LossFunction.MSE)
                        .build(), "lstm5_50")

                .addLayer("lstm1_70", new LSTM.Builder()
                        .nIn(numInputs)
                        .nOut(384)
                        .build(), "input_70")
                .addLayer("lstm2_70", new LSTM.Builder()
                        .nIn(384)
                        .nOut(384)
                        .dropOut(0.6)
                        .build(), "lstm1_70")
                .addLayer("lstm3_70", new LSTM.Builder()
                        .nIn(384)
                        .nOut(384)
                        .build(), "lstm2_70")
                .addLayer("lstm4_70", new LSTM.Builder()
                        .nIn(384)
                        .nOut(256)
                        .dropOut(0.8)
                        .build(), "lstm3_70")
                .addLayer("lstm5_70", new LSTM.Builder()
                        .nIn(256)
                        .nOut(128)
                        .build(), "lstm4_70")
                .addLayer("lstm6_70", new LSTM.Builder()
                        .nIn(128)
                        .nOut(64)
                        .build(), "lstm5_70")
                .addLayer("output_70", new RnnOutputLayer.Builder()
                        .nIn(64)
                        .nOut(numOutputs)
                        .activation(Activation.IDENTITY)
                        .lossFunction(LossFunctions.LossFunction.MSE)
                        .build(), "lstm6_70")


                .addLayer("lstm1_100", new LSTM.Builder()
                        .nIn(numInputs)
                        .nOut(384)
                        .build(), "input_100")
                .addLayer("lstm2_100", new LSTM.Builder()
                        .nIn(384)
                        .nOut(384)
                        .dropOut(0.6)
                        .build(), "lstm1_100")
                .addLayer("lstm3_100", new LSTM.Builder()
                        .nIn(384)
                        .nOut(384)
                        .build(), "lstm2_100")
                .addLayer("lstm4_100", new LSTM.Builder()
                        .nIn(384)
                        .nOut(256)
                        .dropOut(0.8)
                        .build(), "lstm3_100")
                .addLayer("lstm5_100", new LSTM.Builder()
                        .nIn(256)
                        .nOut(256)
                        .dropOut(0.8)
                        .build(), "lstm4_100")
                .addLayer("lstm6_100", new LSTM.Builder()
                        .nIn(256)
                        .nOut(128)
                        .build(), "lstm5_100")
                .addLayer("lstm7_100", new LSTM.Builder()
                        .nIn(128)
                        .nOut(64)
                        .build(), "lstm6_100")
                .addLayer("output_100", new RnnOutputLayer.Builder()
                        .nIn(64)
                        .nOut(numOutputs)
                        .activation(Activation.IDENTITY)
                        .lossFunction(LossFunctions.LossFunction.MSE)
                        .build(), "lstm7_100")
                .tBPTTLength(40)
                .build();
    }

    public void save(String path) {
        if (!Files.isDirectory(Path.of(path))) throw new IllegalStateException("Path must be a directory");
        ModelLoader.saveModel(model, pathToModel);
        accessor.saveAccessor(pathToAccessor);
    }

    public INDArray[] predict(INDArray[] inputs, INDArray[] masks) {
        return model.output(false, inputs, null, masks);
    }

    public double[][][] predict(double[][][] inputs) {
        INDArray[] input = new INDArray[inputs.length];
        INDArray[] masks = new INDArray[inputs.length];

        Arrays.sort(inputs);

        for (int i = 0; i < inputs.length; i++) {
            int length = inputs[i][0].length;

            INDArray newInput = Nd4j.createFromArray(inputs[i]);
            INDArray mask = accessor.getMask(length);

            input[i] = newInput;
            masks[i] = mask;
        }

        INDArray[] predictions = predict(input, masks);

        double[][][] output = new double[predictions.length][][];
        for (int i = 0; i < predictions.length; i++) {
            INDArray arr = predictions[i];
            double[][] matrix = arr.slice(0).toDoubleMatrix();
            int length = 0;
            for (double d : matrix[0]) {
                if (d != 0) length++;
                else if (length>=3) break;
            }
            double[][] newMatrix = new double[numOutputs][length];
            for (int j = 0; j < numOutputs; j++) {
                System.arraycopy(matrix[j], 0, newMatrix[j], 0, length);
            }
            output[i] = newMatrix;
        }

        for (int i = 0; i < input.length; i++) {
            input[i].close();
            masks[i].close();
        }

        for (int i = 0; i < output.length; i++) {
            accessor.revertLabels(inputs[i], output[i]);
        }

        return output;
    }

    public void fit(JointMultiDataSetIterator iterator, int epoch) {
        for (int i = 0; i < epoch; i++) {
            model.fit(iterator);
        }
    }

    public void fit(JointMultiDataSetIterator iterator, int epoch, String path, int countEpoch) {
        for (int i = 0; i < epoch; i++) {
            if (i>0 && i%countEpoch == 0)
                save(path);
            model.fit(iterator);
        }
    }
}
