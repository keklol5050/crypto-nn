package com.crypto.analysis.main.core.arima;


import com.crypto.analysis.main.core.arima.analytics.Arima;
import com.crypto.analysis.main.core.arima.models.ForecastResultModel;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;

import com.crypto.analysis.main.core.data_utils.utils.binance.BinanceDataMultipleInstance;
import com.crypto.analysis.main.core.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.core.model.DataVisualisation;
import com.crypto.analysis.main.core.vo.DataObject;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        DataObject[]  data = BinanceDataMultipleInstance.getLatestInstances(Coin.BTCUSDT, TimeFrame.FIFTEEN_MINUTES, 80, new FundamentalDataUtil());
        double[] dataArr = new double[data.length-6];
        for (int i = 0; i < dataArr.length; i++) {
            double val =  data[i].getCandle().getClose();
            dataArr[i] = val;
        }

        double iqr =  new Percentile(75).evaluate(dataArr) -  new Percentile(25).evaluate(dataArr);
        double[] diff = new double[dataArr.length - 1];
        for (int i = 0; i < dataArr.length - 1; i++) {
            diff[i] = dataArr[i + 1] - dataArr[i];
        }
        double[] standardizedPrices = new double[diff.length];
        for (int i = 0; i < diff.length; i++) {
            standardizedPrices[i] = diff[i] / iqr;
        }

        int forecastSize = 6;

        double[] input = new double[standardizedPrices.length-6];
        System.arraycopy(standardizedPrices, 0, input, 0, input.length);

        double[] real = new double[forecastSize];
        System.arraycopy(standardizedPrices, input.length, real, 0, forecastSize);


        ForecastResultModel forecastResult = Arima.forecast_arima(input, forecastSize);
        System.out.println("Predicted: " + Arrays.toString(forecastResult.getForecast()));
        double[] reverted = new double[forecastSize];
        for (int i = 0; i < reverted.length; i++) {
            reverted[i] = (forecastResult.getForecast()[i] * iqr);
        }
        System.out.println("Predicted reverted: " +Arrays.toString(reverted));
        System.out.println("Real output: " +Arrays.toString(real));
        DataVisualisation.visualizeData("Prediction", "price", "price", input, real, forecastResult.getForecast());
    }
}
