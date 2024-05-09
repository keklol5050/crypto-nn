package com.crypto.analysis.main;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.BinanceDataUtil;
import com.crypto.analysis.main.core.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.core.data_utils.utils.mutils.DataVisualisation;
import com.crypto.analysis.main.core.vo.DataObject;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jfree.data.xy.XYSeries;

public class Test {
    public static void main(String[] args) {
        DataObject[] objs = BinanceDataUtil.getLatestInstances(Coin.BTCUSDT, TimeFrame.ONE_HOUR, 100, new FundamentalDataUtil());
        double[] close = new double[objs.length];
        for (int i = 0; i < close.length; i++) {
            close[i] = objs[i].getCandle().getVolume();
        }


        XYSeries closeSeries = new XYSeries("close");
        for (int i = 0; i < close.length; i++) {
            closeSeries.add(i, close[i]);
        }
        DataVisualisation.visualize("Simple price", "candle", "price", closeSeries);


        double[] diff = new double[objs.length-1];
        for (int i = 0; i < diff.length; i++) {
            diff[i] = close[i+1] - close[i];
        }
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (int i = 0; i < diff.length; i++) {
            stats.addValue(diff[i]);
        }
        double mean = stats.getMean();
        for (int i = 0; i < diff.length; i++) {
            diff[i] = diff[i] - mean;
        }
        XYSeries diffSeries = new XYSeries("diff");
        for (int i = 0; i < diff.length; i++) {
            diffSeries.add(i, diff[i]);
        }
        DataVisualisation.visualize("Simple diff", "candle", "diff", diffSeries);

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

        boolean haveNegativeValues = true;
        while (haveNegativeValues) {
            haveNegativeValues = false;
            for (double doubles : prices) {
                if (doubles <= 0) {
                    haveNegativeValues = true;
                    break;
                }
            }
            if (haveNegativeValues) {
                for (int j = 0; j < prices.length; j++) {
                    prices[j] = prices[j] + 10000;
                }
            }
        }

        for (int i = 0; i < prices.length; i++) {
            prices[i] = Math.log(prices[i]);
        }

        for (double price : prices) {
            stats.addValue(price);
        }
        double mean = stats.getMean();

        double p25 = stats.getPercentile(25);
        double p75 = stats.getPercentile(75);
        double iqr = p75 - p25;

        double[] standardizedPrices = new double[prices.length];
        for (int i = 0; i < prices.length; i++) {
            standardizedPrices[i] = (prices[i] - mean) / iqr;
        }
        return standardizedPrices;
    }

}
