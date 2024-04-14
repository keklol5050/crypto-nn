package com.crypto.analysis.main.core.data.refactor;

import com.crypto.analysis.main.core.data_utils.normalizers.Differentiator;
import com.crypto.analysis.main.core.data_utils.normalizers.RobustScaler;
import com.crypto.analysis.main.core.data_utils.select.StaticData;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.binance.BinanceDataMultipleInstance;
import com.crypto.analysis.main.core.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.core.model.DataVisualisation;
import com.crypto.analysis.main.core.vo.DataObject;
import com.crypto.analysis.main.core.vo.TrainSetElement;
import lombok.Getter;
import org.jfree.data.xy.XYSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import static com.crypto.analysis.main.core.data_utils.select.StaticData.*;

public class DataRefactor {
    private final LinkedList<double[][]> data;

    private final int countInput;
    private final int countOutput;
    private final int delimiter;
    private LinkedList<TrainSetElement> elements;

    @Getter
    private RobustScaler normalizer;

    public DataRefactor(LinkedList<double[][]> data, int countInput, int countOutput, int delimiter) {
        this.data = data;
        this.countInput = countInput;
        this.countOutput = countOutput;
        this.delimiter = delimiter;

        init();
    }
    public void init() {
        elements = new LinkedList<>();

        HashMap<Integer, ArrayList<Double>> volatileData = new HashMap<>();

        for (double[][] datum : data) {
            for (int j = NOT_VOLATILE_VALUES; j < MODEL_NUM_INPUTS; j++) {
                ArrayList<Double> values = new ArrayList<>();
                int start = volatileData.size()!=VOLATILE_VALUES_COUNT_FROM_LAST ? 0 : datum.length-delimiter;
                for (int k = start; k < datum.length; k++) {
                    values.add(datum[k][j]);
                }
                if (volatileData.containsKey(j)) volatileData.get(j).addAll(values);
                else volatileData.put(j, values);
            }
        }
        normalizer = new RobustScaler(MASK_OUTPUT, NOT_VOLATILE_VALUES, StaticData.VOLATILE_VALUES_COUNT_FROM_LAST);
        normalizer.setVolatileData(volatileData);
        normalizer.setHasVolatileChangedFlag(false);
        normalizer.setCountInputs(countInput);
        normalizer.setCountOutputs(countOutput);

        Differentiator differentiator = new Differentiator();

        for (double[][] in : data) {
            if (countInput + countOutput != (in.length-NUMBER_OF_DIFFERENTIATIONS)) throw new ArithmeticException("Parameters count are not equals");
            if (countOutput < 1 || countInput < 1)
                throw new IllegalArgumentException("Parameters cannot be zero or negative");

            double[][] diff = refactor(in, differentiator, false, normalizer);

            double[][] input = new double[countInput][];
            System.arraycopy(diff, 0, input, 0, input.length);

            double[][] output = new double[countOutput][];
            System.arraycopy(diff, input.length, output, 0, output.length);

            double[][] finalOutput = new double[countOutput][];
            for (int i = 0; i < finalOutput.length; i++) {
                finalOutput[i] = new double[MASK_OUTPUT.length];
                for (int j = 0; j < finalOutput[i].length; j++) {
                    finalOutput[i][j] = output[i][MASK_OUTPUT[j]];
                }
            }
            input = Transposer.transpose(input);
            finalOutput = Transposer.transpose(finalOutput, input[0].length);

            normalizer.changeBinding(diff, input);

            elements.add(new TrainSetElement(input, finalOutput));
        }
    }

    public static double[][] refactor(double[][] in, Differentiator differentiator, boolean save, RobustScaler scaler) {
        double[] orient = RobustScaler.getColumn(in, POSITION_OF_PRICES_NORMALIZER_IND, in.length);

        double[][] diff = differentiator.differentiate(in, NUMBER_OF_DIFFERENTIATIONS, save);

        for (int i = COUNT_VALUES_FOR_DIFFERENTIATION; i < COUNT_VALUES_FOR_DIFFERENTIATION+MOVING_AVERAGES_COUNT_FOR_DIFF_WITH_PRICE_VALUES; i++) {
            for (int j = 0; j < diff.length; j++) {
                diff[j][i] = orient[j+NUMBER_OF_DIFFERENTIATIONS] - diff[j][i];
            }
        }

        scaler.fit(diff);
        scaler.transform(diff);

        return diff;
    }

    public LinkedList<TrainSetElement> transform() {
        return elements;
    }

    public static void main(String[] args) {
        DataLength dl = DataLength.X100_10;
        DataObject[] pr = BinanceDataMultipleInstance.getLatestInstances(Coin.BTCUSDT, TimeFrame.FIFTEEN_MINUTES, dl.getCountInput()+dl.getCountOutput(), new FundamentalDataUtil());
        double[][] in = new double[pr.length][];
        for (int i = 0; i < pr.length; i++) {
            in[i] = pr[i].getParamArray();
        }
        for (double[] i : in) {
            System.out.println(Arrays.toString(i));
        }
        System.out.println();
        System.out.println("=============================================================================================");
        System.out.println("=============================================================================================");
        System.out.println();
        LinkedList<double[][]> inl = new LinkedList<double[][]>();
        inl.add(in);

        DataRefactor normalizer = new DataRefactor(inl, dl.getCountInput()-NUMBER_OF_DIFFERENTIATIONS, dl.getCountOutput(),1);
        LinkedList<TrainSetElement> element = normalizer.transform();
        double[][] normalizedData = element.get(0).getData();

        for (double[] row : normalizedData) {
            System.out.println(Arrays.toString(row));
        }
        System.out.println();
        System.out.println();
        double[][] normalizedOutput = element.get(0).getResult();

        for (double[] row : normalizedOutput) {
            System.out.println(Arrays.toString(row));
        }

        System.out.println(normalizedData.length);
        System.out.println(normalizedOutput.length);

        double[] open = new double[normalizedData[0].length];
        System.arraycopy(normalizedData[0], 0, open, 0, normalizedData[0].length);
        XYSeries series1 = new XYSeries("open");
        for (int i = 0; i < open.length; i++) {
            series1.add(i, open[i]);
        }

        double[] high = new double[normalizedData[1].length];
        System.arraycopy(normalizedData[1], 0, high, 0, normalizedData[1].length);
        XYSeries series2 = new XYSeries("high");
        for (int i = 0; i < high.length; i++) {
            series2.add(i, high[i]);
        }

        double[] low = new double[normalizedData[2].length];
        System.arraycopy(normalizedData[2], 0, low, 0, normalizedData[2].length);
        XYSeries series3 = new XYSeries("low");
        for (int i = 0; i < low.length; i++) {
            series3.add(i, low[i]);
        }

        double[] close = new double[normalizedData[13].length];
        System.arraycopy(normalizedData[13], 0, close, 0, normalizedData[13].length);
        XYSeries series4 = new XYSeries("close");
        for (int i = 0; i < close.length; i++) {
            series4.add(i, close[i]);
        }

        DataVisualisation.visualize("OHLC data", "count candles", "normalized diff price", series4);
        normalizer.getNormalizer().revertFeatures(normalizedData);
        normalizer.getNormalizer().revertLabels(normalizedData, normalizedOutput);

        for (double[] row : normalizedData) {
            System.out.println(Arrays.toString(row));
        }
        System.out.println();
        System.out.println();
        for (double[] row : normalizedOutput) {
            System.out.println(Arrays.toString(row));
        }

    }

}