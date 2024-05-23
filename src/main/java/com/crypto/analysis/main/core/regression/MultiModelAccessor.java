package com.crypto.analysis.main.core.regression;

import ai.djl.translate.TranslateException;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.core.vo.DataObject;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MultiModelAccessor {
    private final HashMap<DataLength, ModelAccessor> modelMap = new HashMap<DataLength, ModelAccessor>();

    private final Coin coin;
    private final int numFeatures;
    private final String folderPath;

    private FundamentalDataUtil fdUtil;

    public MultiModelAccessor(Coin coin, int numFeatures, String folderPath) {
        this.coin = coin;
        this.numFeatures = numFeatures;
        this.folderPath = folderPath;

        this.fdUtil = new FundamentalDataUtil();
    }

    public void init() {
        for (DataLength dl : DataLength.values()) {
            RegressionModel model = new RegressionModel(coin, numFeatures, folderPath, dl);
            ModelAccessor modelAccessor = new ModelAccessor(model, fdUtil);
            modelMap.put(dl, modelAccessor);
        }
    }

    public float[][] predictSingle(DataLength dl, TimeFrame tf, DataObject[] data) throws TranslateException {
        return modelMap.get(dl).predict(tf, data);
    }

    public TreeMap<DataLength, float[][]> predictMulti(TimeFrame tf, DataObject[] data) throws TranslateException {
        if (data.length < DataLength.MAX_REG_INPUT_LENGTH)
            throw new IllegalArgumentException("Data length must be more than " + DataLength.MAX_REG_INPUT_LENGTH);

        TreeMap<DataLength, float[][]> result = new TreeMap<DataLength, float[][]>();
        for (DataLength dl : DataLength.values()) {
            result.put(dl, modelMap.get(dl).predict(tf, data));
        }
        return result;
    }

    public float[][] predictAverage(TimeFrame tf, DataObject[] data) throws TranslateException {
        TreeMap<DataLength, float[][]> predictionsMap = predictMulti(tf, data);

        int maxInputSize = DataLength.MAX_REG_INPUT_LENGTH;
        int maxOutputSize = DataLength.MAX_REG_OUTPUT_LENGTH;

        float[][] result = new float[predictionsMap.firstEntry().getValue().length][maxInputSize + maxOutputSize];

        for (int i = 0; i < result.length; i++) {
            float[] mergedInputData = new float[maxInputSize];
            float[] averagedOutputData = new float[maxOutputSize];

            System.arraycopy(predictionsMap.get(DataLength.MAX_LENGTH)[i], 0, mergedInputData, 0, DataLength.MAX_LENGTH.getCountInput());

            for (int j = 0; j < maxOutputSize; j++) {
                float sum = 0;
                int count = 0;
                for (Map.Entry<DataLength, float[][]> entry : predictionsMap.entrySet()) {
                    int currentOutputCountFrom = entry.getValue().length - entry.getKey().getCountOutput();
                    sum += entry.getValue()[i][currentOutputCountFrom + j];
                    count++;
                }
                if (count > 0) {
                    averagedOutputData[i] = sum / count;
                }
            }

            float[] currentResult = new float[mergedInputData.length + averagedOutputData.length];
            System.arraycopy(mergedInputData, 0, currentResult, 0, mergedInputData.length);
            System.arraycopy(averagedOutputData, 0, currentResult, mergedInputData.length, averagedOutputData.length);

            result[i] = currentResult;
        }

        return result;
    }
}
