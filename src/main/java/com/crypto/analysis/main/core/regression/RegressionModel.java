package com.crypto.analysis.main.core.regression;

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
import ai.djl.nn.recurrent.LSTM;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.dataset.ArrayDataset;
import ai.djl.training.dataset.Record;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.initializer.NormalInitializer;
import ai.djl.training.listener.EarlyStoppingListener;
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
import com.crypto.analysis.main.core.vo.ModelParams;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

import static com.crypto.analysis.main.core.vo.DataObject.MASK_OUTPUT;
import static com.crypto.analysis.main.core.vo.ModelParams.manager;

public class RegressionModel {
    @Getter
    private final Coin coin;
    @Getter
    private final int numFeatures;
    @Getter
    private final Path folderPath;
    @Getter
    private final DataLength dataLength;
    @Getter
    private final TimeFrame tf;
    @Getter
    private final ModelParams params;

    private SequentialBlock block;
    private Model model;
    private Trainer trainer;

    private final String name;
    private boolean isInitialized = false;

    private static final Logger logger = LoggerFactory.getLogger(RegressionModel.class);

    public RegressionModel(ModelParams params) {
        this.coin = params.getCoin();
        this.numFeatures = params.getNumFeatures();
        this.folderPath = params.getFolderPath();
        this.dataLength = params.getDataLength();
        this.tf = params.getTimeFrame();
        this.name = params.getModelName();

        this.params = params;
        logger.info(String.format("Got model params: %s", params));
    }

    public void init() { // 1, 200, 3, 2, GRU, 2, 100, 0.05f, GPU, FLOAT32
        if (isInitialized)
            throw new IllegalStateException("Model is already initialized");

        this.block = new SequentialBlock()
                .add(Conv1d.builder()
                        .setKernelShape(new Shape(5))
                        .optStride(new Shape(1))
                        .setFilters(400)
                        .build())
                .add(Activation.tanhBlock())
                .add(Pool.maxPool1dBlock(new Shape(2), new Shape(1)))
                .add(LambdaBlock.singleton(array -> array.swapAxes(1, 2)))
                .add(LSTM.builder()
                        .setNumLayers(2)
                        .setStateSize(200)
                        .optBidirectional(false)
                        .optReturnState(false)
                        .optBatchFirst(true)
                        .build())
                .add(BatchNorm.builder().build())
                .add(Blocks.batchFlattenBlock())
                .add(Linear.builder().setUnits(dataLength.getCountOutput()).build());

        this.model = Model.newInstance(name, params.getDevice());
        this.model.setDataType(params.getDataType());
        this.model.setBlock(block);

        logger.info("Data type: {}", model.getDataType());
        logger.info("Device type: {}", params.getDevice());
        logger.info("Number of features: {}", numFeatures);
        logger.info("Data length class: {}", dataLength);
        logger.info("Time frame class: {}", tf);
        logger.info("Model folder path: {}", folderPath);
        logger.info("Model name: {}", name);
        logger.info("Block created: \n{}", block.toString());
        logger.info("Model created: \n{}", model.toString());

        isInitialized = true;
    }

    public void load() throws MalformedModelException, IOException {
        if (!isInitialized)
            throw new IllegalStateException("Model is not initialized");

        model.load(folderPath);
        logger.info("Model loaded from {}{}", model.getModelPath(), "\\" + model.getName());
        logger.info("Block loaded: \n{}", block.toString());
        logger.info("Model loaded: \n{}", model.toString());
    }

    public Predictor<NDList, NDList> getPredictor() {
        if (!isInitialized)
            throw new IllegalStateException("Model is not initialized");

        return model.newPredictor(new NoopTranslator());
    }

    public void initAndLoad() throws MalformedModelException, IOException {
        init();
        load();
    }

    public void initTrainer() {
        if (!isInitialized)
            throw new IllegalStateException("Model is not initialized");

        this.trainer = this.model.newTrainer(new DefaultTrainingConfig(Loss.l2Loss())
                .addEvaluator(new Accuracy())
                .optOptimizer(Optimizer.adam()
                        .optLearningRateTracker(Tracker.fixed(0.001f))
                        .optWeightDecays(0.000128f)
                        .build())
                .optInitializer(new NormalInitializer(), Parameter.Type.WEIGHT)
                .addTrainingListeners(TrainingListener.Defaults.logging()));

        Shape initShape = new Shape(params.getBatchSize(), numFeatures, dataLength.getCountInput());
        this.trainer.initialize(initShape);

        Metrics metrics = new Metrics();
        this.trainer.setMetrics(metrics);

        logger.info("Trainer initialized with shape {}", initShape);
        logger.info("Metrics: {}", metrics);
        logger.info("Device: {}", params.getDevice());
        logger.info("Block initialized by trainer: \n{}", block.toString());
        logger.info("Model initialized by trainer: \n{}", model.toString());
    }

    public void fit(int numEpochs, ArrayDataset trainSet, ArrayDataset testSet) throws TranslateException, IOException {
        if (!isInitialized)
            throw new IllegalStateException("Model is not initialized");

        System.gc();
        if (numEpochs > 0) {
            logger.info("Start training...");
            try {
                EasyTrain.fit(trainer, numEpochs, trainSet, testSet);
            } catch (EarlyStoppingListener.EarlyStoppedException e) {
                logger.info("Stopped early at epoch {} because: {}", e.getStopEpoch(), e.getMessage());
            }
            this.model = this.trainer.getModel();
            this.model.setProperty("Epoch", String.valueOf(numEpochs));

            logger.info("Training result: \n{}", trainer.getTrainingResult());

            model.save(folderPath, name);
            logger.info("Model saved to {}{}", model.getModelPath(), "\\" + model.getName());
        }
        if (testSet == null)
            return;

        Predictor<NDList, NDList> predictor = getPredictor();
        for (int i = 0; i < testSet.size(); i++) {
            Record pair = testSet.get(manager, i);
            NDList input = pair.getData();

            NDArray in = input.singletonOrThrow();
            float[] close = in.get(MASK_OUTPUT[0]).toFloatArray();
            long[] orig = in.getShape().getShape();
            in = in.reshape(1, orig[0], orig[1]);

            float[] real = pair.getLabels().singletonOrThrow().toFloatArray();
            float[] output = predictor.predict(new NDList(in)).singletonOrThrow().toFloatArray();

            DataVisualisation.visualizeData("Prediction test", "candle", "price", close, real, output);
        }
    }

    public void close() {
        closeTrainer();
        this.model.close();
    }

    public void closeTrainer() {
        this.trainer.close();
    }
}

