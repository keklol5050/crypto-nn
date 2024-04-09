package com.crypto.analysis.main;

public class TestLinear {
    public static void main(String[] args) {
/*
        int numInputs = 80;

        MultiLayerNetwork model = ModelLoader.loadModel("D:\\models\\model_linear_50%.zip");
        model.init();
        model.setListeners(new ScoreIterationListener(10));

        TrainDataSet trainSet = new TrainDataSet("BTCUSDT");
        trainSet.prepareTrainSet();

        LinkedList<double[][]> in = trainSet.getTrainData();
        LinkedList<double[]> out = trainSet.getTrainResult();

        LinkedList<double[]> inputList = new LinkedList<>();
        LinkedList<Double> outputList = new LinkedList<>();

        for (int i = 10000; i<in.size(); i++) {
            double[][] inArr = in.get(i);
            inputList.add(Arrays.stream(inArr).flatMapToDouble(Arrays::stream).map(e->e/10000).toArray());

            outputList.add(out.get(i)[3]/10000);
        }

        int countRight = 0;

        for (int i = 0; i < inputList.size(); i++) {
            INDArray newInput = Nd4j.create(new double[][]{inputList.get(i)});
            INDArray predictedOutput = model.output(newInput, false);
            double prediction = predictedOutput.getDouble(0)*10000;
            double real = outputList.get(i)*10000;
            double delta = real*0.01;
            boolean isRight = Math.abs(prediction - real) < delta;
            if (isRight) countRight++;
            System.out.printf("Predicted: %f, Real: %f, is right (<1%%) :%s ", prediction, real, isRight);
            System.out.println();
        }

        System.out.println("Right: "+countRight + " from " + inputList.size());
        System.out.println("Percentage: " + ((double) countRight/(double) inputList.size()) * 100 + '%');

 */
    }

}
