package com.crypto.analysis.main.core.regression;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.metric.Metrics;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.*;
import ai.djl.nn.convolutional.Conv1d;
import ai.djl.nn.core.Linear;
import ai.djl.nn.norm.BatchNorm;
import ai.djl.nn.norm.Dropout;
import ai.djl.nn.pooling.Pool;
import ai.djl.nn.recurrent.GRU;
import ai.djl.nn.recurrent.LSTM;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.dataset.Record;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.initializer.NormalInitializer;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.optimizer.Optimizer;
import ai.djl.training.tracker.Tracker;
import ai.djl.translate.NoopTranslator;
import ai.djl.translate.TranslateException;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.mutils.DataVisualisation;
import com.crypto.analysis.main.core.ndata.CSVCoinDataSet;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

import static com.crypto.analysis.main.core.data_utils.select.StaticData.MASK_OUTPUT;
import static com.crypto.analysis.main.core.data_utils.select.StaticData.manager;

public class Predictor {
    public static void main(String[] args) throws TranslateException, IOException, MalformedModelException {
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter count of epochs: ");
        int numEpochs = sc.nextInt();
        sc.nextLine();

        System.out.println("Enter data length: ");
        DataLength length = DataLength.valueOf(sc.nextLine());

        System.out.println("Enter time frame: ");
        TimeFrame tf = TimeFrame.valueOf(sc.nextLine());

        Path path = Path.of("D:/Conv1d");

        System.out.println("Enter true/false to load the model: ");
        boolean loadModel = Boolean.parseBoolean(sc.nextLine());

        sc.close();

        CSVCoinDataSet setD = new CSVCoinDataSet(Coin.BTCUSDT, tf);
        setD.load();

        RegressionDataSet regressionDataSet = RegressionDataSet.prepareTrainSet(Coin.BTCUSDT, length, setD);
        RandomAccessDataset trainSet = regressionDataSet.getTrainSet();
        RandomAccessDataset testSet = regressionDataSet.getTestSet();

        int batchSize = regressionDataSet.getBatchSize();
        int numFeatures = regressionDataSet.getNumFeatures();
        int numInputSteps = regressionDataSet.getInputSteps();
        int numOutputSteps = regressionDataSet.getOutputSteps();

        float dropoutProbability = 0.5f;

        SequentialBlock block = new SequentialBlock()
                .add(Conv1d.builder()
                        .setKernelShape(new Shape(3))
                        .optStride(new Shape(1))
                        .setFilters(200)
                        .build())
                .add(Activation.tanhBlock())
                .add(Pool.maxPool1dBlock(new Shape(2), new Shape(1)))
                .add(new LambdaBlock(ndArrays -> {
                    NDList newList = new NDList();
                    for (int i = 0; i < ndArrays.size(); i++) {
                        NDArray array = ndArrays.get(i);
                        array = array.transpose(0, 2, 1);
                        newList.add(array);
                    }
                    return newList;
                }))
                .add(GRU.builder()
                        .setNumLayers(2)
                        .setStateSize(100)
                        .optBidirectional(false)
                        .optReturnState(false)
                        .optBatchFirst(true)
                        .optDropRate(0.05f)
                        .build())
                .add(BatchNorm.builder().build())
                .add(Blocks.batchFlattenBlock())
                .add(Linear.builder().setUnits(numOutputSteps).build());

        Model model = Model.newInstance("Conv1d", Device.gpu());
        model.setDataType(DataType.FLOAT32);
        model.setBlock(block);

        if (loadModel) {
            model.load(path);
        }

        Trainer trainer = model.newTrainer(new DefaultTrainingConfig(Loss.l2Loss())
                .addEvaluator(new Accuracy())
                .optOptimizer(Optimizer.adam()
                        .optLearningRateTracker(Tracker.fixed(0.001f))
                        .optWeightDecays(0.0001f)
                        .build())
                .optInitializer(new NormalInitializer(), Parameter.Type.WEIGHT)
                .optDevices(new Device[]{Device.gpu()})
                .addTrainingListeners(TrainingListener.Defaults.logging()));

        trainer.initialize(new Shape(batchSize, numFeatures, numInputSteps));

        Metrics metrics = new Metrics();
        trainer.setMetrics(metrics);

        System.out.println("Count input params: " + regressionDataSet.getNumFeatures());
        System.out.println("Count output params: " + regressionDataSet.getOutputSteps());
        System.out.println("Count input objects: " + regressionDataSet.getInputSteps());
        System.out.println("Data length: " + length);
        System.out.println("Time frame: " + tf);
        System.out.println("Number of epochs: " + numEpochs);
        System.out.println("Model path: " + path);

        EasyTrain.fit(trainer, numEpochs, trainSet, testSet);

        model = trainer.getModel();
        model.setProperty("Epoch", String.valueOf(numEpochs));

        System.out.println(trainer.getTrainingResult());
        System.out.println(model.getDataType());
        model.save(path, "Conv1d");

        ai.djl.inference.Predictor<NDList, NDList> predictor = model.newPredictor(new NoopTranslator());
        for (int i = 0; i < 200; i++) {
            Record pair = testSet.get(manager, i);
            NDList input = pair.getData();

            NDArray in = input.singletonOrThrow();
            float[] close = in.get(MASK_OUTPUT[0]).toFloatArray();
            long[] orig = in.getShape().getShape();
            in = in.reshape(1, orig[0], orig[1]);

            float[] real = pair.getLabels().singletonOrThrow().toFloatArray();
            float[] output = predictor.predict(new NDList(in)).singletonOrThrow().toFloatArray();

            DataVisualisation.visualizeData("Prediction", "candle length", "price", close, real, output);
        }

        model.close();
        trainer.close();
        manager.close();
    }
}

