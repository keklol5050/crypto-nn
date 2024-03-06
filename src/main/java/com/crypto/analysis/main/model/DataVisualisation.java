package com.crypto.analysis.main.model;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;

import javax.swing.*;
import java.util.List;

public class DataVisualisation {

    private final List<DataSet> sets;

    public DataVisualisation(List<DataSet> sets) {
        this.sets = sets;
    }

    public void plotDataset() {

        XYSeriesCollection c = getXySeriesCollection();

        String title = "Training Data";
        String xAxisLabel = "xAxisLabel";
        String yAxisLabel = "yAxisLabel";
        PlotOrientation orientation = PlotOrientation.VERTICAL;
        boolean legend = false;
        boolean tooltips = false;
        boolean urls = false;
        //noinspection ConstantConditions
        JFreeChart chart = ChartFactory.createScatterPlot(title, xAxisLabel, yAxisLabel, c, orientation, legend, tooltips, urls);
        JPanel panel = new ChartPanel(chart);

        JFrame f = new JFrame();
        f.add(panel);
        f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        f.pack();
        f.setTitle("Training Data");

        f.setVisible(true);
    }
    private XYSeriesCollection getXySeriesCollection() {
        XYSeriesCollection c = new XYSeriesCollection();
        int dscounter = 1; //use to name the dataseries
        for (DataSet ds : sets) {
            INDArray features = ds.getFeatures();
            INDArray outputs = ds.getLabels();

            int nRows = features.rows();
            XYSeries series = new XYSeries("S" + dscounter++);
            for (int i = 0; i < nRows; i++) {
                series.add(features.getDouble(i), outputs.getDouble(i));
            }

            c.addSeries(series);
        }
        return c;
    }
}
