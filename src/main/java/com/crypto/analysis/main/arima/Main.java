package com.crypto.analysis.main.arima;


import com.crypto.analysis.main.arima.analytics.Arima;
import com.crypto.analysis.main.arima.models.ForecastResultModel;
import com.crypto.analysis.main.data_utils.select.coin.Coin;
import com.crypto.analysis.main.data_utils.select.coin.TimeFrame;

import com.crypto.analysis.main.data_utils.utils.binance.BinanceDataMultipleInstance;
import com.crypto.analysis.main.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.ndata.CSVCoinDataSet;
import com.crypto.analysis.main.vo.DataObject;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        DataObject[]  data = BinanceDataMultipleInstance.getLatestInstances(Coin.BTCUSDT, TimeFrame.FIFTEEN_MINUTES, 100, new FundamentalDataUtil());
        double[] dataArr = new double[data.length];
        for (int i = 0; i < dataArr.length; i++) {
            double val =  data[i].getCandle().getClose();
            dataArr[i] = val;
        }
        double median = new Percentile(50).evaluate(dataArr);
        double iqr =  new Percentile(75).evaluate(dataArr) -  new Percentile(25).evaluate(dataArr);

        double[] standardizedPrices = new double[dataArr.length];
        for (int i = 0; i < dataArr.length; i++) {
            double val = (dataArr[i] - median) / iqr;
            standardizedPrices[i] = val;
            System.out.println(val);
        }
        int forecastSize = 3;

        ForecastResultModel forecastResult = Arima.forecast_arima(standardizedPrices, forecastSize);

        System.out.println("Predicted: " + Arrays.toString(forecastResult.getForecast()));
        double[] reverted = new double[forecastSize];
        for (int i = 0; i < reverted.length; i++) {
            reverted[i] = (forecastResult.getForecast()[i] * iqr) + median;
        }
        System.out.println("Predicted reverted: " +Arrays.toString(reverted));
        System.out.println(Arrays.toString(forecastResult.getlowerBound()));
        System.out.println(Arrays.toString(forecastResult.getupperBound()));
        System.out.println(forecastResult.getRMSE());
        System.out.println(forecastResult.getMaxNormalizedVariance());
        System.out.println();

    }
}
