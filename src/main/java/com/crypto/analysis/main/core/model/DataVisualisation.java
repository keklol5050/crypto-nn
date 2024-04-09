package com.crypto.analysis.main.core.model;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;

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

        int indexCopy = index-1;

        XYSeries realSeries = new XYSeries("Real");
        for (double y : real) {
            realSeries.add(indexCopy++, y);
        }

        indexCopy = index-1;

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

}
