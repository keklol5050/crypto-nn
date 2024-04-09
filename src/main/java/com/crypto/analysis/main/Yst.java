package com.crypto.analysis.main;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.binance.BinanceDataMultipleInstance;
import com.crypto.analysis.main.core.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.core.vo.DataObject;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;

public class Yst {
    static Percentile p25 = new Percentile(25);
    static Percentile p50 = new Percentile(50);
    static Percentile p75 = new Percentile(75);

    public static void main(String[] args) {
        DataLength dl = DataLength.X100_10;
        DataObject[] data =  BinanceDataMultipleInstance.getLatestInstances(Coin.BTCUSDT, TimeFrame.FIFTEEN_MINUTES, dl.getCountInput()+dl.getCountOutput(), new FundamentalDataUtil());
        double[] bitcoinPrices = new double[data.length];
        for (int i = 0; i < bitcoinPrices.length; i++) {
            double val = data[i].getCandle().getClose();
            bitcoinPrices[i] = val;
        }

        // Стационаризация
        double[] detrendedPrices = detrend(bitcoinPrices);

        // Стандартизация
        double[] standardizedPrices = standardize(bitcoinPrices);

        double[] seasonisedPrices = removeSeasonality(bitcoinPrices, 4);

        double[] robustedPrices = robustStandardize(bitcoinPrices);

        double[] nPrices = nStandardize(bitcoinPrices);
        double[] sPrices = sStandardize(bitcoinPrices);
        double[] sTrans = sTransform(bitcoinPrices);

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
        System.out.println("\nNormalized Bitcoin Prices:");
        for (double price : nPrices) {
            System.out.println(price);
        }

        XYSeries series1 = new XYSeries("nPrices");
        for (int i = 0; i < nPrices.length; i++) {
            series1.add(i, nPrices[i]);
        }
        XYSeries series2 = new XYSeries("sTrans");
        for (int i = 0; i < sTrans.length; i++) {
            series2.add(i, sTrans[i]);
        }

        XYSeriesCollection c = new XYSeriesCollection();
        c.addSeries(series1);

        String title = "Training Data";
        String xAxisLabel = "xAxisLabel";
        String yAxisLabel = "yAxisLabel";
        PlotOrientation orientation = PlotOrientation.VERTICAL;
        boolean legend = true;
        boolean tooltips = true;
        boolean urls = true;
        //noinspection ConstantConditions
        JFreeChart chart = ChartFactory.createScatterPlot(title, xAxisLabel, yAxisLabel, c, orientation, legend, tooltips, urls);
        XYPlot plot = chart.getXYPlot();

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        plot.setRenderer(renderer);

        ChartPanel panel = new ChartPanel(chart);

        JFrame f = new JFrame();
        f.add(panel);
        f.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        f.pack();
        f.setTitle("Training Data");

        f.setVisible(true);
    }

    public static double[] sTransform(double[] prices) {
        double[] transformedData = new double[prices.length];
        for (int i = 0; i < prices.length; i++) {
            transformedData[i] = Math.log(prices[i]);
        }
        return transformedData;
    }

    public static double[] nStandardize(double[] prices) {
        double[] transformedData = new double[prices.length];
        for (int i = 0; i < prices.length; i++) {
            transformedData[i] = (Math.pow(prices[i], 0.2) - 1) / 0.2;
        }

        double[] diff = new double[transformedData.length - 1];
        for (int i = 0; i < transformedData.length - 1; i++) {
            diff[i] = transformedData[i + 1] - transformedData[i];
        }

        double[] diff2 = new double[diff.length - 1];
        for (int i = 0; i < diff.length - 1; i++) {
            diff2[i] = diff[i + 1] - diff[i];
        }

        double[] diff3 = new double[diff2.length - 1];
        for (int i = 0; i < diff2.length - 1; i++) {
            diff3[i] = diff2[i + 1] - diff2[i];
        }

        double[] diff4 = new double[diff3.length - 1];
        for (int i = 0; i < diff3.length - 1; i++) {
            diff4[i] = diff3[i + 1] - diff3[i];
        }

        double[] diff5 = new double[diff4.length - 1];
        for (int i = 0; i < diff4.length - 1; i++) {
            diff5[i] = diff4[i + 1] - diff4[i];
        }
        double[] diff6 = new double[diff5.length - 1];
        for (int i = 0; i < diff5.length - 1; i++) {
            diff6[i] = diff5[i + 1] - diff5[i];
        }
        double[] diff7 = new double[diff6.length - 1];
        for (int i = 0; i < diff6.length - 1; i++) {
            diff7[i] = diff6[i + 1] - diff6[i];
        }
        double[] diff8 = new double[diff7.length - 1];
        for (int i = 0; i < diff7.length - 1; i++) {
            diff8[i] = diff7[i + 1] - diff7[i];
        }
        double[] diff9 = new double[diff8.length - 1];
        for (int i = 0; i < diff8.length - 1; i++) {
            diff9[i] = diff8[i + 1] - diff8[i];
        }
        double[] diff10 = new double[diff9.length - 1];
        for (int i = 0; i < diff9.length - 1; i++) {
            diff10[i] = diff9[i + 1] - diff9[i];
        }

        double median = p50.evaluate(diff10);
        double iqr = p75.evaluate(diff10) - p25.evaluate(diff10);

        double[] standardizedPrices = new double[diff10.length];
        for (int i = 0; i < diff10.length; i++) {
            standardizedPrices[i] = (diff10[i] - median) / iqr;
        }

        return standardizedPrices;
    }

    public static double[] sStandardize(double[] prices) {
        double iqr = p75.evaluate(prices) - p25.evaluate(prices);

        double[] diff = new double[prices.length - 1];
        for (int i = 0; i < prices.length - 1; i++) {
            diff[i] = prices[i + 1] - prices[i];
        }

        double[] standardizedPrices = new double[diff.length];
        for (int i = 0; i < diff.length; i++) {
            standardizedPrices[i] = diff[i] / iqr;
        }

        return standardizedPrices;
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

