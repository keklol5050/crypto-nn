package com.crypto.analysis.main;

public class BoxCox {

    private double lambda;

    public BoxCox(double lambda) {
        this.lambda = lambda;
    }

    public double[] transform(double[] data) {
        double[] transformedData = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            if (lambda == 0) {
                transformedData[i] = Math.log(data[i]);
            } else {
                transformedData[i] = (Math.pow(data[i], lambda) - 1) / lambda;
            }
        }
        return transformedData;
    }

    public double[] inverseTransform(double[] data) {
        double[] originalData = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            if (lambda == 0) {
                originalData[i] = Math.exp(data[i]);
            } else {
                originalData[i] = Math.pow(lambda * data[i] + 1, 1 / lambda);
            }
        }
        return originalData;
    }

    public static void main(String[] args) {
        // Ваши данные о ценах биткоина
        double[] bitcoinPrices = {32000, 33000, 34000, 35000, 36000, 37000, 38000, 39000, 40000};

// Создание экземпляра класса BoxCox с lambda = 0.5
        BoxCox boxCox = new BoxCox(0.5);

// Преобразование данных
        double[] transformedData = boxCox.transform(bitcoinPrices);

// Вывод преобразованных данных
        for (double data : transformedData) {
            System.out.println(data);
        }

// Восстановление исходных данных
        double[] originalData = boxCox.inverseTransform(transformedData);

// Вывод исходных данных
        for (double data : originalData) {
            System.out.println(data);
        }
    }
}
