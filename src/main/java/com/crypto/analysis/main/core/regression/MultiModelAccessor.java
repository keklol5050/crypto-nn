package com.crypto.analysis.main.core.regression;

import ai.djl.MalformedModelException;
import ai.djl.translate.TranslateException;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.vo.DataObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MultiModelAccessor {
    private HashMap<TimeFrame, HashMap<DataLength, ModelAccessor>> modelMap;
    private final Coin coin;

    public MultiModelAccessor(Coin coin) {
        this.coin = coin;
    }

    public void init(Initializer initializer) throws MalformedModelException, IOException {
        if (modelMap != null && !modelMap.isEmpty())
            return;

        modelMap = initializer.getAllModelAccessors();
    }

    public float[] predictSingle(DataLength dl, TimeFrame tf, DataObject[] data) throws TranslateException {
        return modelMap.get(tf).get(dl).predict(data);
    }

    public TreeMap<DataLength, float[]> predictMulti(TimeFrame tf, DataObject[] data) throws TranslateException {
        if (data.length < DataLength.MAX_REG_INPUT_LENGTH)
            throw new IllegalArgumentException("Data length must be more than " + DataLength.MAX_REG_INPUT_LENGTH);

        TreeMap<DataLength, float[]> result = new TreeMap<DataLength, float[]>();
        for (DataLength dl : DataLength.values()) {
            result.put(dl, modelMap.get(tf).get(dl).predict(data));
        }
        return result;
    }

    public float[] predictAverage(TimeFrame tf, DataObject[] data) throws TranslateException {
        TreeMap<DataLength, float[]> predictionsMap = predictMulti(tf, data);

        int maxOutputSize = DataLength.MAX_REG_OUTPUT_LENGTH;

        float[] averagedOutputData = new float[maxOutputSize];

        for (int i = 0; i < maxOutputSize; i++) {
            float sum = 0;
            int count = 0;
            for (Map.Entry<DataLength, float[]> entry : predictionsMap.entrySet()) {
                if (i < entry.getValue().length) {
                    sum += entry.getValue()[i];
                    count++;
                }
            }
            if (count > 0) {
                averagedOutputData[i] = sum / count;
            }
        }

        return averagedOutputData;
    }
}
