package com.crypto.analysis.main.data.refactor;

import com.crypto.analysis.main.data_utils.normalizers.BatchNormalizer;
import com.crypto.analysis.main.data_utils.select.StaticData;
import com.crypto.analysis.main.data_utils.select.coin.Coin;
import com.crypto.analysis.main.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.data_utils.utils.binance.BinanceDataMultipleInstance;
import com.crypto.analysis.main.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.vo.DataObject;
import com.crypto.analysis.main.vo.TrainSetElement;
import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedList;

import static com.crypto.analysis.main.data_utils.select.StaticData.MASK_OUTPUT;

public class DataRefactor {
    private final LinkedList<double[][]> data;
    private final int countInput;
    private final int countOutput;
    private final int sequenceLength;

    private LinkedList<TrainSetElement> elements;

    @Getter
    private BatchNormalizer normalizer;

    public DataRefactor(LinkedList<double[][]> data, int countInput, int countOutput) {
        this.data = data;
        this.countInput = countInput;
        this.countOutput = countOutput;
        this.sequenceLength = data.get(0)[0].length;
        init();
    }

    public void init() {
        elements = new LinkedList<>();

        normalizer = new BatchNormalizer(MASK_OUTPUT, countInput, countOutput,
                sequenceLength-StaticData.VOLATILE_VALUES_COUNT_FROM_LAST);
        normalizer.fitHorizontal(data);
        normalizer.transformHorizontal(data);

        for (double[][] in : data) {
            if (countInput+countOutput!=in.length) throw new ArithmeticException("Parameters count are not equals");
            if (countOutput<1 || countInput<1) throw new IllegalArgumentException("Parameters cannot be zero or negative");

            double[][] input = new double[countInput][];
            System.arraycopy(in, 0, input, 0, input.length);

            double[][] output = new double[countOutput][];
            System.arraycopy(in,countInput, output, 0, output.length);

            double[][] finalOutput = new double[countOutput][];
            for (int i = 0; i < finalOutput.length; i++) {
                finalOutput[i] = new double[MASK_OUTPUT.length];
                for (int j = 0; j < finalOutput[i].length; j++) {
                    finalOutput[i][j] = output[i][MASK_OUTPUT[j]];
                }
            }
            input = Transposer.transpose(input);
            finalOutput = Transposer.transpose(finalOutput, input[0].length);

            normalizer.changeBinding(in, input);

            elements.add(new TrainSetElement(input, finalOutput));
        }
    }

    public LinkedList<TrainSetElement> transform() {
        return elements;
    }


    public static void main(String[] args) {
        DataObject[] pr = BinanceDataMultipleInstance.getLatestInstances(Coin.BTCUSDT, TimeFrame.ONE_HOUR, 30, new FundamentalDataUtil());
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
        DataRefactor normalizer = new DataRefactor(inl, 25, 5);
        LinkedList<TrainSetElement> element = normalizer.transform();
        double[][] normalizedData = element.get(0).getData();

        for (double[] row : normalizedData) {
            System.out.println(Arrays.toString(row));
        }
        System.out.println();
        System.out.println();
        double[][] normalizedOutput =  element.get(0).getResult();

        for (double[] row : normalizedOutput) {
            System.out.println(Arrays.toString(row));
        }

        System.out.println(normalizedData.length);
        System.out.println(normalizedOutput.length);

        normalizer.getNormalizer().revertFeaturesVertical(normalizedData);
        normalizer.getNormalizer().revertLabelsVertical(normalizedData, normalizedOutput);

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