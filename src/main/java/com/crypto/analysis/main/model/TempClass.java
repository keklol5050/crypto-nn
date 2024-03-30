package com.crypto.analysis.main.model;

import com.crypto.analysis.main.data.train.TrainDataSet;
import com.crypto.analysis.main.data_utils.normalizers.RobustNormalizer;
import com.crypto.analysis.main.data_utils.select.StaticData;
import com.crypto.analysis.main.data_utils.select.coin.Coin;
import com.crypto.analysis.main.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.ndata.CSVCoinDataSet;
import org.deeplearning4j.datasets.iterator.JointMultiDataSetIterator;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

import java.util.*;

public class TempClass {
    private final String path;
    private final CSVCoinDataSet csvSet1h;
    private final JointMultiDataSetIterator trainIterator;
    private RelativeAccessor accessor;
    private HashMap<Integer, TrainDataSet> trainList;
    private Model model;

    public TempClass(CSVCoinDataSet csvSet1h, String path) {
        this.csvSet1h = csvSet1h;
        this.path = path;

        trainIterator = getDataSetIterator();
    }

    public static void main(String[] args) throws Exception {
        new TempClass(new CSVCoinDataSet(Coin.BTCUSDT, TimeFrame.ONE_HOUR),
                "D:\\md\\").start();
    }

    private JointMultiDataSetIterator getDataSetIterator() {

        LinkedList<DataSetIterator> iteratorsTrain = new LinkedList<>();

        HashMap<Integer, INDArray> masks = new HashMap<>();

        HashMap<Integer, RobustNormalizer> normalizers = new HashMap<>();

        trainList = new HashMap<>();

        csvSet1h.load();

        for (DataLength dl : DataLength.values()) {
            TrainDataSet trainSet = TrainDataSet.prepareTrainSet(Coin.BTCUSDT, dl, csvSet1h);

            LinkedList<double[][]> inputList = trainSet.getTrainData();
            LinkedList<double[][]> outputList = trainSet.getTrainResult();

            int sequenceLength = inputList.get(0)[0].length;

            INDArray labelsMask = Nd4j.zeros(1, sequenceLength);
            for (int i = 0; i < dl.getCountOutput(); i++) {
                labelsMask.putScalar(new int[]{0, i}, 1.0);
            }

            List<DataSet> sets = new ArrayList<>();
            for (int i = 0; i < inputList.size(); i++) {
                double[][] inputData = inputList.get(i);
                double[][] outputData = outputList.get(i);

                INDArray input = Nd4j.createFromArray(new double[][][]{inputData});
                INDArray labels = Nd4j.createFromArray(new double[][][]{outputData});

                DataSet set = new DataSet(input, labels, null, labelsMask);
                sets.add(set);
            }
            Collections.shuffle(sets);
            DataSetIterator it = new ListDataSetIterator<>(sets, 64);

            iteratorsTrain.add(it);
            masks.put(sequenceLength, labelsMask);
            normalizers.put(sequenceLength, trainSet.getNormalizer());

            trainList.put(sequenceLength, trainSet);
        }
        accessor = new RelativeAccessor(masks, normalizers);
        return new JointMultiDataSetIterator(iteratorsTrain.toArray(new DataSetIterator[0]));
    }

    public void start() {
        model = new Model(path);
        model.init();
        model.setAccessor(accessor);
        model.fit(trainIterator, 9999, 3);
        test();
    }

    private void test() {
        long start = System.currentTimeMillis();
        LinkedList<double[][]> d50values = trainList.get(50).getTestData();
        LinkedList<double[][]> d70values = trainList.get(70).getTestData();
        LinkedList<double[][]> d100values = trainList.get(100).getTestData();

        LinkedList<double[][]> d50result = trainList.get(50).getTestResult();
        LinkedList<double[][]> d70result = trainList.get(70).getTestResult();
        LinkedList<double[][]> d100result = trainList.get(100).getTestResult();

        int pred50acc = 0;
        int pred70acc = 0;
        int pred100acc = 0;

        for (int i = 0; i < d100result.size(); i++) {
            double[][][] nIn = new double[3][][];
            nIn[0] = d50values.get(i);
            nIn[1] = d70values.get(i);
            nIn[2] = d100values.get(i);

            double[][][] predicted = model.predict(nIn);

            accessor.revertFeatures(d50values.get(i));
            accessor.revertFeatures(d70values.get(i));
            accessor.revertFeatures(d100values.get(i));

            accessor.revertLabels(d50values.get(i), d50result.get(i));
            accessor.revertLabels(d70values.get(i), d70result.get(i));
            accessor.revertLabels(d100values.get(i), d100result.get(i));

            HashMap<Integer, double[][]> data = new HashMap<Integer, double[][]>();
            HashMap<Integer, double[][]> results = new HashMap<Integer, double[][]>();
            HashMap<Integer, double[][]> real = new HashMap<Integer, double[][]>();
            int numOutputs = StaticData.MODEL_NUM_OUTPUTS;

            results.put(50, predicted[0]);
            results.put(70, predicted[1]);
            results.put(100, predicted[2]);

            double[][] newMatrix50 = new double[numOutputs][3];
            for (int j = 0; j < numOutputs; j++) {
                System.arraycopy(d50result.get(i)[j], 0, newMatrix50[j], 0, 3);
            }
            real.put(50, newMatrix50);

            double[][] newMatrix70 = new double[numOutputs][6];
            for (int j = 0; j < numOutputs; j++) {
                System.arraycopy(d70result.get(i)[j], 0, newMatrix70[j], 0, 6);
            }
            real.put(70, newMatrix70);

            double[][] newMatrix100 = new double[numOutputs][9];
            for (int j = 0; j < numOutputs; j++) {
                System.arraycopy(d100result.get(i)[j], 0, newMatrix100[j], 0, 9);
            }
            real.put(100, newMatrix100);

            data.put(50, d50values.get(i));
            data.put(70, d70values.get(i));
            data.put(100, d100values.get(i));

            for (DataLength dl : DataLength.values()) {
                int length = dl.getCountInput();
                double[][] inputData = data.get(length);
                double[][] realResult = real.get(length);
                double[][] predResult = results.get(length);

                for (int x = 0; x < 4; x++) {
                    double[] d = inputData[x];
                    System.out.println(Arrays.toString(d));
                }
                System.out.println("====== Real result: ======");
                for (double[] d : realResult) {
                    System.out.println(Arrays.toString(d));
                }
                System.out.println("====== Predicted result: ======");
                for (double[] d : predResult) {
                    System.out.println(Arrays.toString(d));
                }

                int rows = realResult.length;
                int cols = realResult[0].length;
                double totalDifference = 0.0;

                for (int k = 0; k < rows; k++) {
                    for (int j = 0; j < cols; j++) {
                        double realValue = realResult[k][j];
                        double predictedValue = predResult[k][j];
                        double percentageDifference = Math.abs(predictedValue - realValue);
                        totalDifference += percentageDifference;
                    }
                }
                totalDifference /= rows * cols;
                double mean = predResult[2][0] * 0.005;
                if (totalDifference < mean) {
                    switch (length) {
                        case 50 -> pred50acc++;
                        case 70 -> pred70acc++;
                        case 100 -> pred100acc++;
                    }
                }
                System.out.println("Average difference " + totalDifference + ", mean: " + mean);
                System.out.println();
                System.out.println("================================================================================================");
                System.out.println();
            }
        }
        System.out.println("Percentage accuracy 50 input size: " + ((double) pred50acc / (double) d100result.size()) * 100 + '%');
        System.out.println("Percentage accuracy 70 input size: " + ((double) pred70acc / (double) d100result.size()) * 100 + '%');
        System.out.println("Percentage accuracy 100 input size: " + ((double) pred100acc / (double) d100values.size()) * 100 + '%');
        System.out.println("Time taken: " + ((System.currentTimeMillis() - start) / 1000) / 60 + " minutes");
    }
}
