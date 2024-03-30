package com.crypto.analysis.main.data_utils.normalizers;

import com.crypto.analysis.main.data_utils.select.coin.Coin;
import com.crypto.analysis.main.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.data_utils.utils.binance.BinanceDataMultipleInstance;
import com.crypto.analysis.main.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.vo.DataObject;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;


public class VerticalNormalizer implements Serializable {
    private final LinkedList<double[][]> data = new LinkedList<>();
    private double[] min;
    private double[] max;

    public static VerticalNormalizer loadNormalizer(String basePath) throws Exception {
        if (basePath == null || basePath.isEmpty()) throw new NullPointerException();
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(basePath));
        return (VerticalNormalizer) in.readObject();
    }

    public static void main(String[] args) {
        DataObject[] objs = BinanceDataMultipleInstance.getLatestInstances(Coin.BTCUSDT, TimeFrame.ONE_HOUR, 40, new FundamentalDataUtil());
        double[][] m = new double[objs.length][];
        double[][] first = new double[objs.length / 2][];
        double[][] second = new double[objs.length / 2][];
        for (int i = 0; i < objs.length; i++) {
            double[] val = objs[i].getParamArray();
            m[i] = val;
        }

        System.arraycopy(m, 0, first, 0, first.length);
        System.arraycopy(m, first.length, second, 0, second.length);
        LinkedList<double[][]> list = new LinkedList<double[][]>();
        list.add(first);
        list.add(second);
        VerticalNormalizer normalizer = new VerticalNormalizer();
        normalizer.fit(list);
        System.out.println(Arrays.toString(normalizer.max));
        System.out.println(Arrays.toString(normalizer.min));
        System.out.println();
        for (double[] d : first) {
            System.out.println(Arrays.toString(d));
        }
        normalizer.transform(list);
        list.forEach(e -> {
            for (double[] doubles : e) System.out.println(Arrays.toString(doubles));
        });
        System.out.println();
        System.out.println();

        System.out.println();
        normalizer.revert(list.get(0));
        for (double[] e : list.get(0)) {
            System.out.println(Arrays.toString(e));
        }
    }

    public void fit(LinkedList<double[][]> data) {
        if (data == null || data.isEmpty()) throw new IllegalArgumentException("Input list cannot be empty");
        if (data.get(0).length == 0 || data.get(0)[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");

        this.data.addAll(data);
        int arrayCapacity = data.getFirst().length;
        int paramCapacity = data.getFirst()[0].length;

        min = new double[paramCapacity];
        max = new double[paramCapacity];

        LinkedList<double[]> minList = new LinkedList<>();
        LinkedList<double[]> maxList = new LinkedList<>();

        for (double[][] dataArr : this.data) {
            if (dataArr.length != arrayCapacity) throw new IllegalArgumentException("Arrays lengths are not equals");

            double[] min = new double[paramCapacity];
            double[] max = new double[paramCapacity];
            for (int i = 0; i < paramCapacity; i++) {
                double valueMin = Double.MAX_VALUE;
                double valueMax = Double.MIN_VALUE;
                for (int j = 0; j < arrayCapacity; j++) {
                    if (dataArr[j].length != paramCapacity)
                        throw new IllegalArgumentException("Arrays parameters are not equals");

                    valueMin = Math.min(valueMin, dataArr[j][i]);
                    valueMax = Math.max(valueMax, dataArr[j][i]);
                }
                min[i] = valueMin;
                max[i] = valueMax;
            }
            minList.add(min);
            maxList.add(max);
        }

        for (int i = 0; i < paramCapacity; i++) {
            double valueMin = Double.MAX_VALUE;
            double valueMax = Double.MIN_VALUE;
            for (int j = 0; j < minList.size(); j++) {
                valueMin = Math.min(valueMin, minList.get(j)[i]);
                valueMax = Math.max(valueMax, maxList.get(j)[i]);
            }
            min[i] = valueMin;
            max[i] = valueMax;
        }
    }

    public void transform(LinkedList<double[][]> input) {
        if (input == null || input.isEmpty() || input.get(0).length == 0 || input.get(0)[0].length == 0)
            throw new IllegalArgumentException("Input list cannot be empty");
        if (input.get(0)[0].length != min.length)
            throw new IllegalArgumentException("Input array parameters are not right");

        for (double[][] in : input) {
            transform(in);
        }
    }

    public void transform(double[][] in) {
        if (in == null || in.length == 0 || in[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");
        if (in[0].length != min.length) throw new IllegalArgumentException("Input array parameters are not right");

        for (int i = 0; i < in[0].length; i++) {
            for (int j = 0; j < in.length; j++) {
                in[j][i] = ((in[j][i] - min[i]) / (max[i] - min[i]));
            }
        }
    }

    public void revert(double[][] input) {
        if (input == null || input.length == 0 || input[0].length == 0)
            throw new IllegalArgumentException("Input array cannot be empty");
        if (input[0].length != min.length) throw new IllegalArgumentException("Input array parameters are not right");

        for (int i = 0; i < input[0].length; i++) {
            for (int j = 0; j < input.length; j++) { //X=Xstd⋅(Xmax−Xmin)+Xmin
                input[j][i] = (input[j][i]) * (max[i] - min[i]) + min[i];
            }
        }
    }

    public void saveNormalizer(String basePath) throws Exception {
        if (basePath == null || basePath.isEmpty()) throw new NullPointerException();
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(basePath));
        out.writeObject(this);
        System.out.println("Normalizer saved on the path: " + basePath);
    }
}
