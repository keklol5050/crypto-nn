package com.crypto.analysis.main.core.regression;

import ai.djl.MalformedModelException;
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
import com.crypto.analysis.main.core.data_utils.utils.mutils.DataVisualisation;
import com.crypto.analysis.main.core.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.core.vo.DataObject;
import org.jfree.data.xy.XYSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.crypto.analysis.main.core.data_utils.select.StaticData.*;

public class ModelAccessor {
    private final Coin coin;
    private final Predictor<NDList, NDList> predictor;
    private final DataLength dl;
    private final MaxAbsScaler normalizer;
    private final FundamentalDataUtil fdUtil;

    private static final Logger logger = LoggerFactory.getLogger(ModelAccessor.class);

    public ModelAccessor(RegressionModel model, FundamentalDataUtil fdUtil) {
        this.coin = model.getCoin();
        this.predictor = model.getPredictor();
        this.dl = model.getDl();
        this.fdUtil = fdUtil;

        this.normalizer = new MaxAbsScaler(MASK_OUTPUT, dl.getCountOutput(), MODEL_NUM_INPUTS);

        logger.info(String.format("Model accessor created for coin %s with DL class %s", coin, dl));
    }

    public float[][] predict(TimeFrame tf, DataObject[] data) throws TranslateException {
        if (fdUtil != null)
            fdUtil.init();

        int countInput = dl.getCountInput();
        int countOutput = dl.getCountOutput();

        if (data == null) {
            data = BinanceDataUtil.getLatestInstances(coin, tf, countOutput, fdUtil);
        } else {
            if (data.length != countInput) {
                DataObject[] newData = new DataObject[countInput];
                if (data.length - (data.length - countInput) >= 0)
                    System.arraycopy(data, data.length - countInput, newData, data.length - countInput, data.length - (data.length - countInput));
                data = newData;
            }
        }

        float[][] dataArr = new float[data.length][];
        for (int i = 0; i < data.length; i++) {
            dataArr[i] = data[i].getParamArray();
        }

        float firstOpen = dataArr[0][0];

        RegressionDataSet.refactor(dataArr, true, normalizer);
        float[][] newInputArr = Transposer.transpose(dataArr);

        NDArray input = manager.create(newInputArr);
        long[] orig = input.getShape().getShape();
        input = input.reshape(1, orig[0], orig[1]);

        float[][] output = Transposer.transpose(new float[][]{predictor.predict(new NDList(input)).singletonOrThrow().toFloatArray()});

        normalizer.revertFeatures(dataArr);
        normalizer.revertLabels(dataArr, output);

        float[][] in = new float[dataArr.length + output.length][MASK_OUTPUT.length];
        for (int i = 0; i < dataArr.length; i++) {
            for (int j = 0; j < MASK_OUTPUT.length; j++) {
                in[i][j] = dataArr[i][MASK_OUTPUT[j]];
            }
        }
        for (int i = dataArr.length; i < in.length; i++) {
            for (int j = 0; j < MASK_OUTPUT.length; j++) {
                in[i][j] = output[i - dataArr.length][j];
            }
        }

        in = Transposer.transpose(in);

        output = new float[][]{in[0]}; // for mask

        return output;
    }


    public static void main(String[] args) throws MalformedModelException, IOException, TranslateException {
        Coin coin = Coin.BTCUSDT;
        DataLength dl = DataLength.L120_6;
        RegressionModel model = new RegressionModel(coin, MODEL_NUM_INPUTS, "D:/Conv1d", dl);
        model.initAndLoad();
        ModelAccessor accessor = new ModelAccessor(model, null);

        DataObject[] data = BinanceDataUtil.getLatestInstances(Coin.BTCUSDT, TimeFrame.ONE_HOUR, dl.getCountInput(), null);

        float[] pred = accessor.predict(TimeFrame.ONE_HOUR, data)[0];
        XYSeries series = new XYSeries("Predicted");
        for (int i = 0; i < pred.length; i++) {
            series.add(i, pred[i]);
        }
        DataVisualisation.visualize("Prediction", "count", "price", series);
    }

}