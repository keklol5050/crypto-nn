package com.crypto.analysis.main.core.regression;

import ai.djl.training.dataset.ArrayDataset;
import ai.djl.translate.TranslateException;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.ndata.CSVCoinDataSet;

import java.io.IOException;
import java.util.HashMap;

public class Initializer {
    public final HashMap<DataLength, RegressionModel> modelMap = new HashMap<DataLength, RegressionModel>();

    private final Coin coin;
    private final int numFeatures;
    private final int batchSize;
    private final int numEpochs;
    private final String folderPath;

    public Initializer(Coin coin, int numFeatures, int batchSize, int numEpochs, String folderPath) {
        this.coin = coin;
        this.numFeatures = numFeatures;
        this.batchSize = batchSize;
        this.numEpochs = numEpochs;
        this.folderPath = folderPath;
    }

    public void addModel(DataLength dl) throws TranslateException, IOException {
        CSVCoinDataSet csvSet = new CSVCoinDataSet(coin, TimeFrame.ONE_HOUR);
        csvSet.load();

        RegressionDataSet set = RegressionDataSet.prepareTrainSet(coin, dl, csvSet);
        ArrayDataset trainSet = set.getTrainSet();
        ArrayDataset testSet = set.getTestSet();

        RegressionModel model = new RegressionModel(coin, numFeatures, folderPath, dl);
        model.init();
        model.initTrainer(batchSize);
        model.fit(numEpochs, trainSet, testSet);

        modelMap.put(dl, model);
    }

    public void prepareAllModels() {
        for (DataLength dl : DataLength.values()) {
            try {
                addModel(dl);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
