package com.crypto.analysis.main;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

public class Yst {
    static Percentile p25 = new Percentile(25);
    static Percentile p50 = new Percentile(50);
    static Percentile p75 = new Percentile(75);

    public static void main(String[] args) {
        // Пример данных цен биткоина
        double[] bitcoinPrices = {10000, 12500, 13000, 16800, 14000, 16700, 16050, 18000, 19000, 20080, 21000, 22000};

        // Стационаризация
        double[] detrendedPrices = detrend(bitcoinPrices);

        // Стандартизация
        double[] standardizedPrices = standardize(bitcoinPrices);

        double[] seasonisedPrices = removeSeasonality(bitcoinPrices, 3);

        double[] robustedPrices = robustStandardize(bitcoinPrices);

        // Вывод результатов
        System.out.println("Detrended Bitcoin Prices:");
        for (double price : detrendedPrices) {
            System.out.println(price);
        }
        System.out.println("\nStandardized Bitcoin Prices:");
        for (double price : standardizedPrices) {
            System.out.println(price);
        }
        System.out.println("\nSeasonalized Bitcoin Prices:");
        for (double price : seasonisedPrices) {
            System.out.println(price);
        }
        System.out.println("\nRobust Standardized Bitcoin Prices:");
        for (double price : robustedPrices) {
            System.out.println(price);
        }

    }

    public static double[] robustStandardize(double[] prices) {
        double median = p50.evaluate(prices);
        double iqr = p75.evaluate(prices) - p25.evaluate(prices);

        double[] standardizedPrices = new double[prices.length];
        for (int i = 0; i < prices.length; i++) {
            standardizedPrices[i] = (prices[i] - median) / iqr;
        }

        return standardizedPrices;
    }


    public static double[] detrend(double[] prices) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (double price : prices) {
            stats.addValue(price);
        }
        double mean = stats.getMean();
        double[] detrendedPrices = new double[prices.length];
        for (int i = 0; i < prices.length; i++) {
            detrendedPrices[i] = prices[i] - mean;
        }
        return detrendedPrices;
    }


    public static double[] standardize(double[] prices) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (double price : prices) {
            stats.addValue(price);
        }
        double mean = stats.getMean();
        double stdDev = stats.getStandardDeviation();
        double[] standardizedPrices = new double[prices.length];
        for (int i = 0; i < prices.length; i++) {
            standardizedPrices[i] = (prices[i] - mean) / stdDev;
        }
        return standardizedPrices;
    }


    public static double[] removeSeasonality(double[] data, int seasonality) {
        // Вычисляем среднее значение для каждого периода сезонности
        double[] seasonalComponent = new double[seasonality];
        for (int i = 0; i < data.length; i++) {
            seasonalComponent[i % seasonality] += data[i];
        }
        for (int i = 0; i < seasonality; i++) {
            seasonalComponent[i] /= ((double) data.length / seasonality);
        }

        // Создаем новый временной ряд без сезонной составляющей
        double[] deseasonalizedData = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            deseasonalizedData[i] = data[i] - seasonalComponent[i % seasonality];
        }
        return deseasonalizedData;
    }

}

