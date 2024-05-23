package com.crypto.analysis.main.core.regression;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.metric.Metrics;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.*;
import ai.djl.nn.convolutional.Conv1d;
import ai.djl.nn.core.Linear;
import ai.djl.nn.norm.BatchNorm;
import ai.djl.nn.pooling.Pool;
import ai.djl.nn.recurrent.GRU;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.dataset.ArrayDataset;
import ai.djl.training.dataset.RandomAccessDataset;
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
import com.crypto.analysis.main.core.ndata.CSVCoinDataSet;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

import static com.crypto.analysis.main.core.data_utils.select.StaticData.DATA_TYPE;
import static com.crypto.analysis.main.core.data_utils.select.StaticData.DEVICE;

public class RegressionModel {
    @Getter
    private final Coin coin;
    @Getter
    private final int numFeatures;
    @Getter
    private final Path folderPath;
    @Getter
    private final DataLength dl;

    private SequentialBlock block;
    private Model model;
    private Trainer trainer;

    private String name;
    private boolean isInitialized = false;

    private static final Logger logger = LoggerFactory.getLogger(RegressionModel.class);

    public RegressionModel(Coin coin, int numFeatures, String folderPath, DataLength dl) {
        this.coin = coin;
        this.numFeatures = numFeatures;
        this.folderPath = Path.of(folderPath);
        this.dl = dl;

        this.name = String.format("Model-%s-%s", coin, dl);
    }

    public void init() {
        if (isInitialized)
            throw new IllegalStateException("Model is already initialized");

        this.block = new SequentialBlock()
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
                .add(Linear.builder().setUnits(dl.getCountOutput()).build());

        this.model = Model.newInstance(name, DEVICE);
        this.model.setDataType(DATA_TYPE);
        this.model.setBlock(block);

        logger.info("Data type: " + model.getDataType());
        logger.info("Device type: " + DEVICE);
        logger.info("Number of features: " + numFeatures);
        logger.info("Data length class: " + dl);
        logger.info("Model folder path: " + folderPath);
        logger.info("Model name: " + name);
        logger.info("Block created: \n" + block.toString());
        logger.info("Model created: \n" + model.toString());

        isInitialized = true;
    }

    public void load() throws MalformedModelException, IOException {
        if (!isInitialized)
            throw new IllegalStateException("Model is not initialized");

        model.load(folderPath);
        logger.info("Model loaded from " + folderPath + name);
        logger.info("Block loaded: \n" + block.toString());
        logger.info("Model loaded: \n" + model.toString());
    }

    public Predictor<NDList, NDList> getPredictor() {
        return model.newPredictor(new NoopTranslator());
    }

    public void initAndLoad() throws MalformedModelException, IOException {
        init();
        load();
    }

    public void initTrainer(int batchSize) {
        this.trainer = this.model.newTrainer(new DefaultTrainingConfig(Loss.l2Loss())
                .addEvaluator(new Accuracy())
                .optOptimizer(Optimizer.adam()
                        .optLearningRateTracker(Tracker.fixed(0.001f))
                        .optWeightDecays(0.0001f)
                        .build())
                .optInitializer(new NormalInitializer(), Parameter.Type.WEIGHT)
                .optDevices(new Device[]{DEVICE})
                .addTrainingListeners(TrainingListener.Defaults.logging()));

        Shape initShape = new Shape(batchSize, numFeatures, dl.getCountInput());
        this.trainer.initialize(initShape);
        logger.info("Trainer initialized with shape " + initShape);
        logger.info("Device: " + DEVICE);

        Metrics metrics = new Metrics();
        this.trainer.setMetrics(metrics);
    }

    public void fit(int numEpochs, ArrayDataset trainSet, ArrayDataset testSet) throws TranslateException, IOException {
        if (numEpochs < 0)
            throw new IllegalArgumentException("Number of epochs must be greater than 0");
        if (!isInitialized)
            throw new IllegalStateException("Model is not initialized");

        logger.info("Start training...");
        EasyTrain.fit(trainer, numEpochs, trainSet, testSet);

        this.model = this.trainer.getModel();
        this.model.setProperty("Epoch", String.valueOf(numEpochs));

        logger.info("Training result: \n" + trainer.getTrainingResult());

        model.save(folderPath, name);
        logger.info("Model saved to " + folderPath + name);
    }

    public static void main(String[] args) throws MalformedModelException, IOException, TranslateException {
        CSVCoinDataSet setD = new CSVCoinDataSet(Coin.BTCUSDT, TimeFrame.ONE_HOUR);
        setD.load();

        RegressionDataSet regressionDataSet = RegressionDataSet.prepareTrainSet(Coin.BTCUSDT, DataLength.L120_6, setD);
        ArrayDataset trainSet = regressionDataSet.getTrainSet();
        ArrayDataset testSet = regressionDataSet.getTestSet();

        int batchSize = regressionDataSet.getBatchSize();
        int numFeatures = regressionDataSet.getNumFeatures();
        int numInputSteps = regressionDataSet.getInputSteps();
        int numOutputSteps = regressionDataSet.getOutputSteps();

        RegressionModel model = new RegressionModel(Coin.BTCUSDT, numFeatures, "D:/Conv1d", DataLength.L120_6);
        model.init();
        model.load();
        model.initTrainer(batchSize);
        model.getPredictor();
        model.fit(100, trainSet, testSet);
    }
}

