package com.crypto.analysis.main.core.data_utils.utils.mutils;

import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class DataVisualisation {

    public static void visualize(String title, String xAxisLabel, String yAxisLabel, XYSeries... values) {
        XYSeriesCollection c = new XYSeriesCollection();
        for (XYSeries value : values) {
            c.addSeries(value);
        }

        PlotOrientation orientation = PlotOrientation.VERTICAL;
        boolean legend = true;
        boolean tooltips = true;
        boolean urls = true;

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

    public static void visualizeData(String title, String xAxisLabel, String yAxisLabel, double[] input, double[] real, double[] predicted) {
        XYSeriesCollection c = new XYSeriesCollection();

        int index = 0;

        XYSeries inputSeries = new XYSeries("Input");
        for (double y : input) {
            inputSeries.add(index++, y);
        }

        int indexCopy = index - 1;

        XYSeries realSeries = new XYSeries("Real");
        for (double y : real) {
            realSeries.add(indexCopy++, y);
        }

        indexCopy = index - 1;

        XYSeries predictedSeries = new XYSeries("Predicted");
        for (double y : predicted) {
            predictedSeries.add(indexCopy++, y);
        }

        c.addSeries(inputSeries);
        c.addSeries(realSeries);
        c.addSeries(predictedSeries);

        PlotOrientation orientation = PlotOrientation.VERTICAL;
        boolean legend = true;
        boolean tooltips = true;
        boolean urls = true;

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

    public static void visualizeChart(double[] open, double[] high, double[] low, double[] close, Date firstDate, TimeFrame tf, int countOutput) {
        if (open.length != low.length || open.length != high.length || open.length != close.length)
            throw new IllegalArgumentException("Data arrays length not equals");

        DateAxis domainAxis = new DateAxis("Date");
        NumberAxis rangeAxis = new NumberAxis("Price");
        String title = "Chart prediction";
        rangeAxis.setAutoRangeIncludesZero(false);

        Date[] date = new Date[open.length];
        double[] volume = new double[open.length];

        date[0] = firstDate;
        Date lastDate = firstDate;
        for (int i = 1; i < open.length; i++) {
            date[i] = new Date(lastDate.getTime() + ((long) tf.getMinuteCount() * 60 * 1000));
            lastDate = date[i];
        }

        XYDataset dataset = new DefaultHighLowDataset("Prediction", date, high, low, open, close, volume);
        CandlestickRenderer renderer = new CandlestickRenderer() {
            @Override
            public Paint getItemPaint(int row, int column) {
                if (column >= dataset.getItemCount(row) - countOutput) {
                    return Color.BLUE;
                } else {
                    return super.getItemPaint(row, column);
                }
            }
        };
        XYPlot mainPlot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        mainPlot.setDomainPannable(true);
        mainPlot.setRangePannable(true);

        int index = dataset.getItemCount(0) - 6;

        XYPointerAnnotation pointer = new XYPointerAnnotation("", dataset.getX(0, index).doubleValue(), dataset.getYValue(0, index), Math.PI / 2);
        pointer.setBaseRadius(30);
        pointer.setTipRadius(5);
        pointer.setArrowLength(5);
        pointer.setArrowWidth(5);
        pointer.setArrowPaint(Color.BLUE);
        pointer.setTextAnchor(TextAnchor.BOTTOM_CENTER);
        mainPlot.addAnnotation(pointer);

        JFreeChart chart = new JFreeChart(title, null, mainPlot, false);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));

        JFrame f = new JFrame();
        f.add(chartPanel);
        f.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        f.pack();
        f.setTitle(title);

        f.setVisible(true);
    }
}
