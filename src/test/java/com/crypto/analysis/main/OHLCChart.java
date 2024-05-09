package com.crypto.analysis.main;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.BinanceDataUtil;
import com.crypto.analysis.main.core.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.core.vo.CandleObject;
import com.crypto.analysis.main.core.vo.DataObject;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

public class OHLCChart extends JFrame {
    public OHLCChart(String title) {
        super(title);
        final DateAxis domainAxis = new DateAxis("Date");
        NumberAxis rangeAxis = new NumberAxis("Price");
        rangeAxis.setAutoRangeIncludesZero(false);
        XYDataset dataset = getDataSet();
        CandlestickRenderer renderer = new CandlestickRenderer() {
            @Override
            public Paint getItemPaint(int row, int column) {
                // Проверяем, является ли свеча одной из последних 5
                if (column >= dataset.getItemCount(row) - 5) {
                    return Color.BLUE; // Изменяем цвет на красный
                } else {
                    return super.getItemPaint(row, column);
                }
            }
        };
        XYPlot mainPlot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);

        JFreeChart chart = new JFreeChart(title, null, mainPlot, false);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 300));

        add(chartPanel);
        pack();
    }

    private OHLCDataset getDataSet() {
        Date[] date = new Date[100];
        double[] high = new double[100];
        double[] low = new double[100];
        double[] open = new double[100];
        double[] close = new double[100];
        double[] volume = new double[100];

        ArrayList<CandleObject> data = BinanceDataUtil.getCandles(Coin.BTCUSDT, TimeFrame.ONE_HOUR, 100);
        for (int i = 0; i < data.size(); i++) {
            CandleObject object = data.get(i);
            date[i] = object.getOpenTime();
            high[i] = object.getHigh();
            low[i] = object.getLow();
            open[i] = object.getOpen();
            close[i] = object.getClose();
            volume[i] = 0;
        }

        return new DefaultHighLowDataset("Series 1", date, high, low, open, close, volume);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            OHLCChart demo = new OHLCChart("Candlestick Demo");
            demo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            demo.setVisible(true);
        });
    }
}
