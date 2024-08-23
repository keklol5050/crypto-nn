package com.crypto.analysis.main.core.regression;

import ai.djl.MalformedModelException;
import ai.djl.training.dataset.ArrayDataset;
import ai.djl.translate.TranslateException;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.PropertiesUtil;
import com.crypto.analysis.main.core.ndata.CSVCoinDataSet;
import com.crypto.analysis.main.core.vo.ModelParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class Initializer {
    public static final Path DEFAULT_FOLDER_PATH = Path.of(new File(Initializer.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParent() + PropertiesUtil.getProperty("model.default_path"));
    public static final int[] DEFAULT_NUM_EPOCHS = PropertiesUtil.getPropertyAsIntegerArray("model.default_epochs");
    public static final int DEFAULT_UPDATE_EPOCHS = PropertiesUtil.getPropertyAsInteger("model.update_epochs");

    private final HashMap<TimeFrame, HashMap<DataLength, RegressionModel>> modelMap = new HashMap<>();
    private HashMap<TimeFrame, HashMap<DataLength, ModelAccessor>> modelAccessors;

    private final Coin coin;
    private final Path folderPath;

    private static final Logger logger = LoggerFactory.getLogger(Initializer.class);

    public Initializer(Coin coin, Path folderPath) {
        this.coin = coin;

        if (folderPath == null)
            throw new IllegalArgumentException("Folder path must be not null");

        this.folderPath = folderPath;
    }

    public void createModel(TimeFrame tf, DataLength dl) throws TranslateException, IOException {
        if (DataLength.values().length != DEFAULT_NUM_EPOCHS.length)
            throw new IllegalArgumentException("Number of time frames must be equal to number of epochs array length");

        int numEpochs = DEFAULT_NUM_EPOCHS[dl.ordinal()];

        logger.info(String.format("Creating model for %s, epochs: %d, folder path: %s",
                dl, numEpochs, folderPath));

        if (numEpochs <= 0)
            throw new IllegalArgumentException("Number of epochs must be greater than 0");

        ModelParams params = ModelParams.getDefault(coin, tf, dl, folderPath);

        CSVCoinDataSet csv = new CSVCoinDataSet(coin, tf);
        csv.load();

        RegressionDataSet regressionDataSet = RegressionDataSet.prepareTrainSet(coin, dl, csv, params.getDevice(), params.getBatchSize(), true);
        ArrayDataset trainSet = regressionDataSet.getTrainSet();
        ArrayDataset testSet = regressionDataSet.getTestSet();

        RegressionModel model = new RegressionModel(params);
        model.init();
        model.initTrainer();
        model.fit(numEpochs, trainSet, testSet);

        model.close();
        System.gc();
    }

    public void createAllModels() throws IOException {
        if (!modelMap.isEmpty()) {
            logger.warn("Models already created or loaded");
            return;
        }
        for (TimeFrame tf : TimeFrame.values()) {
            for (DataLength dl : DataLength.values()) {
                try {
                    createModel(tf, dl);
                    System.gc();

                    logger.info("Created model for time frame {}, length {}", tf, dl);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void loadAllModels() throws MalformedModelException, IOException {
        if (!modelMap.isEmpty()) {
            logger.warn("Models already loaded");
            return;
        }

        logger.info("Loading models from {}", folderPath);
        if (Files.notExists(folderPath))
            throw new IllegalArgumentException(String.format("Folder %s does not exist", folderPath));

        for (TimeFrame tf : TimeFrame.values()) {
            for (DataLength dl : DataLength.values()) {
                ModelParams params = ModelParams.getDefault(coin, tf, dl, folderPath);

                RegressionModel model = new RegressionModel(params);
                model.initAndLoad();

                if (modelMap.containsKey(tf)) {
                    modelMap.get(tf).put(dl, model);
                } else {
                    HashMap<DataLength, RegressionModel> dlMap = new HashMap<>();
                    dlMap.put(dl, model);
                    modelMap.put(tf, dlMap);
                }

                logger.info("Loaded model for length {}, time frame {}", dl, tf);
            }
        }
        logger.info("Loaded all models from {}", folderPath);
    }

    public HashMap<TimeFrame, HashMap<DataLength, ModelAccessor>> getAllModelAccessors() throws MalformedModelException, IOException {
        if (modelAccessors != null && !modelAccessors.isEmpty())
            return modelAccessors;

        loadAllModels();

        HashMap<TimeFrame, HashMap<DataLength, ModelAccessor>> modelAccessors = new HashMap<>();
        for (TimeFrame tf : TimeFrame.values()) {
            for (DataLength dl : DataLength.values()) {
                ModelAccessor modelAccessor = new ModelAccessor(modelMap.get(tf).get(dl), tf);
                if (modelAccessors.containsKey(tf)){
                   modelAccessors.get(tf).put(dl, modelAccessor);
                } else {
                    HashMap<DataLength, ModelAccessor> dlMap = new HashMap<>();
                    dlMap.put(dl, modelAccessor);
                    modelAccessors.put(tf, dlMap);
                }
            }
        }

        this.modelAccessors = modelAccessors;
        return modelAccessors;
    }

    public void updateModel(TimeFrame tf, DataLength dl) throws TranslateException, IOException {
        logger.info("Updating model for length {}", dl);
        if (!modelMap.containsKey(tf) || !modelMap.get(tf).containsKey(dl))
            throw new IllegalStateException("Model does not exist for length " + dl);

        RegressionModel model = modelMap.get(tf).get(dl);
        ModelParams params = model.getParams();

        CSVCoinDataSet csv = new CSVCoinDataSet(coin, tf);
        csv.load();

        RegressionDataSet regressionDataSet = RegressionDataSet.prepareTrainSet(coin, dl, csv, params.getDevice(), params.getBatchSize(), false);
        ArrayDataset trainSet = regressionDataSet.getTrainSet();
        ArrayDataset testSet = regressionDataSet.getTestSet();

        model.initTrainer();
        model.fit(DEFAULT_UPDATE_EPOCHS, trainSet, testSet);

        model.closeTrainer();
        System.gc();

        logger.info("Updated model for length {}", dl);
    }

    public void updateAllModels() throws TranslateException, IOException {
        logger.info("Updating all models");
        for (TimeFrame tf : TimeFrame.values()) {
            for (DataLength dl : DataLength.values()) {
                updateModel(tf, dl);
            }
        }
        logger.info("Updated all models");
    }
}
