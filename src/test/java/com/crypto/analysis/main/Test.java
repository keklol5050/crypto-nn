package com.crypto.analysis.main;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.BinanceDataUtil;
import com.crypto.analysis.main.core.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.core.model.DataVisualisation;
import com.crypto.analysis.main.core.vo.DataObject;
import org.jfree.data.xy.XYSeries;

public class Test {
    public static void main(String[] args) {
        DataObject[] objs = BinanceDataUtil.getLatestInstances(Coin.BTCUSDT, TimeFrame.ONE_HOUR, 100, new FundamentalDataUtil());
        double[] close = new double[objs.length];
        for (int i = 0; i < close.length; i++) {
            close[i] = objs[i].getCandle().getClose();
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
        XYSeries diffSeries = new XYSeries("diff");
        for (int i = 0; i < diff.length; i++) {
            diffSeries.add(i, diff[i]);
        }
        DataVisualisation.visualize("Simple diff", "candle", "diff", diffSeries);
        double[] logDiff = new double[close.length-1];
        for (int i = 0; i < logDiff.length; i++) {
            logDiff[i] = Math.log(close[i+1]) - Math.log(close[i]);
        }
        XYSeries logDiffSeries = new XYSeries("log diff");
        for (int i = 0; i < logDiff.length; i++) {
            logDiffSeries.add(i, logDiff[i]);
        }
        DataVisualisation.visualize("Simple log diff", "candle", "log diff", logDiffSeries);
    }
}
