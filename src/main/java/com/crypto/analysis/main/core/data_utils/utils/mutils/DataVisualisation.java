package com.crypto.analysis.main.core.data_utils.utils.mutils;

import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import kotlin.Pair;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
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

    public static void visualizeQuantiles(float[] input, float[] median, Pair<float[], float[]>... quantiles) {
        YIntervalSeries inputSeries = new YIntervalSeries("Input");
        for (int i = 0; i < input.length; i++) {
            inputSeries.add(i, input[i], input[i], input[i]);
        }

        ArrayList<YIntervalSeries> quantileSeries = new ArrayList<>();
        for (Pair<float[], float[]> pair : quantiles) {
            YIntervalSeries series = new YIntervalSeries("Quantile");
            for (int i = 0; i < pair.getFirst().length; i++) {
                series.add(i+input.length, median[i], pair.getFirst()[i], pair.getSecond()[i]);
            }
            quantileSeries.add(series);
        }
        YIntervalSeriesCollection dataset = new YIntervalSeriesCollection();
        dataset.addSeries(inputSeries);
        for (YIntervalSeries series : quantileSeries) {
            dataset.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Projected Values - Test",          // chart title
                "Date",                   // x axis label
                "Index Projection",       // y axis label
                dataset,                  // data
                true,                     // include legend
                true,                     // tooltips
                false                     // urls
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(false);
        plot.setInsets(new RectangleInsets(5, 5, 5, 20));

        DeviationRenderer renderer = new DeviationRenderer(true, false);

        renderer.setSeriesStroke(0, new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        renderer.setSeriesFillPaint(0, new Color(255, 200, 200));

        int curr = 50;
        for (int i = 0; i < quantileSeries.size(); i++) {
            renderer.setSeriesStroke(i+1, new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            renderer.setSeriesFillPaint(i+1,new Color(curr, 255, 255-curr));
            curr+=55;
        }
        plot.setRenderer(renderer);

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setAutoRangeIncludesZero(false);
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        JFrame f = new JFrame();
        f.add(new ChartPanel(chart));
        f.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        f.pack();
        f.setTitle("Training Data");

        f.setVisible(true);
    }

    public static void visualizeData(String title, String xAxisLabel, String yAxisLabel, float[] input, float[] real, float[] predicted) {
        XYSeriesCollection c = new XYSeriesCollection();

        int index = 0;

        XYSeries inputSeries = new XYSeries("Input");
        for (double y : input) {
            inputSeries.add(index++, y);
        }

        int indexCopy = index;

        XYSeries realSeries = new XYSeries("Real");
        for (double y : real) {
            realSeries.add(indexCopy++, y);
        }

        indexCopy = index;

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
