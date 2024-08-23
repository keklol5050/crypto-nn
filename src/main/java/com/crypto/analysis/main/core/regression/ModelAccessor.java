package com.crypto.analysis.main.core.regression;

import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.translate.TranslateException;
import com.crypto.analysis.main.core.data_utils.normalizers.MaxAbsScaler;
import com.crypto.analysis.main.core.data_utils.normalizers.Transposer;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.BinanceDataUtil;
import com.crypto.analysis.main.core.vo.DataObject;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.crypto.analysis.main.core.vo.DataObject.MASK_OUTPUT;
import static com.crypto.analysis.main.core.vo.ModelParams.DEFAULT_NUM_FEATURES;
import static com.crypto.analysis.main.core.vo.ModelParams.manager;

public class ModelAccessor {
    private final Coin coin;
    private final Predictor<NDList, NDList> predictor;
    private final DataLength dl;
    private final TimeFrame tf;
    private final MaxAbsScaler normalizer;

    private static final Logger logger = LoggerFactory.getLogger(ModelAccessor.class);

    public ModelAccessor(RegressionModel model, TimeFrame tf) {
        this.coin = model.getCoin();
        this.predictor = model.getPredictor();
        this.dl = model.getDataLength();
        this.tf = tf;

        this.normalizer = new MaxAbsScaler(MASK_OUTPUT, dl.getCountOutput(), DEFAULT_NUM_FEATURES);

        logger.info(String.format("Model accessor created for coin %s with DL class %s", coin, dl));
    }

    public float[] predict(DataObject[] data) throws TranslateException {
        int countInput = dl.getCountInput();
        int countOutput = dl.getCountOutput();

        if (data == null) {
            data = BinanceDataUtil.getLatestInstances(coin, tf, countOutput);
        } else if (data.length != countInput) {
            if (data.length > countInput) {
                DataObject[] newData = new DataObject[countInput];
                System.arraycopy(data, data.length - countInput, newData, 0, countInput);
                data = newData;
            } else
                throw new IllegalArgumentException("Invalid number of input objects");
        }

        float[][] dataArr = new float[data.length][];
        for (int i = 0; i < data.length; i++) {
            dataArr[i] = data[i].getParamArray();
        }

        DescriptiveStatistics statistics = new DescriptiveStatistics();
        for (float[] f : dataArr) {
            statistics.addValue(f[MASK_OUTPUT[0]]);
        }

        float closePriceAvg = (float) statistics.getMean();

        dataArr = RegressionDataSet.refactor(dataArr, normalizer);
        float[][] newInputArr = Transposer.transpose(dataArr);

        NDArray input = manager.create(newInputArr);
        long[] orig = input.getShape().getShape();
        input = input.reshape(1, orig[0], orig[1]);

        float[][] output = Transposer.transpose(new float[][]{predictor.predict(new NDList(input)).singletonOrThrow().toFloatArray()});

        normalizer.revertFeatures(dataArr);
        normalizer.revertLabels(dataArr, output);

        float[] fOut = Transposer.transpose(output)[0];
        for (int i = 0; i < fOut.length; i++) {
            fOut[i] = fOut[i] + closePriceAvg;
        }

        return fOut;
    }

}