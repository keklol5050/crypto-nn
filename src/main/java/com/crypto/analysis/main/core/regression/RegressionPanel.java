package com.crypto.analysis.main.core.regression;

import ai.djl.MalformedModelException;
import ai.djl.translate.TranslateException;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.BinanceDataUtil;
import com.crypto.analysis.main.core.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.core.updater.MainUpdater;
import com.crypto.analysis.main.core.vo.CandleObject;
import com.crypto.analysis.main.core.vo.DataObject;
import lombok.Getter;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.crypto.analysis.main.core.regression.Initializer.DEFAULT_FOLDER_PATH;

public class RegressionPanel extends JPanel{
    private final JTabbedPane chartTabbedPane  = new JTabbedPane();

    private MainUpdater updater;
    private FundamentalDataUtil fdUtil;
    @Getter
    private HashMap<Coin, Initializer> initializers;
    private HashMap<Coin, MultiModelAccessor> accessors;

    private TreeMap<TimeFrame, DataObject[]> originalData = new TreeMap<>();
    private TreeMap<Coin, TreeMap<TimeFrame, TreeMap<DataLength, float[]>>> singleData = new TreeMap<>();
    private TreeMap<Coin, TreeMap<TimeFrame, float[]>> averageData = new TreeMap<>();

    public RegressionPanel() {
        init();
    }

    private void init() {
        updater = new MainUpdater();
        fdUtil = new FundamentalDataUtil();

        initializers = new HashMap<>();
        for (Coin coin : Coin.values()) {
            if (coin == Coin.BTCDOMUSDT) continue;
            initializers.put(coin, new Initializer(coin, DEFAULT_FOLDER_PATH));
        }

        accessors = new HashMap<>();
        for (Coin coin : Coin.values()) {
            if (coin == Coin.BTCDOMUSDT) continue;
            MultiModelAccessor accessor = new MultiModelAccessor(coin);
            accessors.put(coin, accessor);
        }

        this.setLayout(new BorderLayout());
        add(chartTabbedPane, BorderLayout.CENTER);
        initGridPanel();
//        new Thread(() -> {
//            updater.updateData(fdUtil);
//        }).start();
    }

    public void refreshData() throws TranslateException {
        for (Map.Entry<Coin, MultiModelAccessor> entry : accessors.entrySet()) {
            try {
                entry.getValue().init(initializers.get(entry.getKey()));
            } catch (MalformedModelException | IOException e) {
                throw new RuntimeException(e);
            }
        }

        singleData = new TreeMap<>();
        averageData = new TreeMap<>();
        originalData = new TreeMap<>();
        for (Coin coin : Coin.values()) {
            if (coin == Coin.BTCDOMUSDT) continue;
            TreeMap<TimeFrame, TreeMap<DataLength, float[]>> singleDataMap = new TreeMap<>();
            TreeMap<TimeFrame, float[]> avgDataMap = new TreeMap<>();

            for (TimeFrame tf : TimeFrame.values()) {
                DataObject[] data = BinanceDataUtil.getLatestInstances(coin, tf, DataLength.MAX_REG_INPUT_LENGTH);
                originalData.put(tf, data);

                MultiModelAccessor accessor = accessors.get(coin);

                TreeMap<DataLength, float[]> predicts = accessor.predictMulti(tf, data);
                singleDataMap.put(tf, predicts);

                float[] averagePredict = accessor.predictAverage(tf, data);
                avgDataMap.put(tf, averagePredict);

            }

            singleData.put(coin, singleDataMap);
            averageData.put(coin, avgDataMap);
        }
    }

    public void refreshCharts() {
        chartTabbedPane.removeAll();
        for (Coin coin : Coin.values()) {
            if (coin == Coin.BTCDOMUSDT) continue;
            JTabbedPane upperTabbedPane = new JTabbedPane();
            TreeMap<TimeFrame, TreeMap<DataLength, float[]>> tfDataMap = singleData.get(coin);

            for (Map.Entry<TimeFrame, TreeMap<DataLength, float[]>> entry : tfDataMap.entrySet()) {
                TimeFrame tf = entry.getKey();
                JTabbedPane subTabbedPane = new JTabbedPane();

                for (Map.Entry<DataLength, float[]> entryInside : entry.getValue().entrySet()) {
                    DataLength dl = entryInside.getKey();
                    float[] predicts = entryInside.getValue();

                    ChartPanel panel = createChartPanel(predicts, String.format("Chart %s %s", tf.getTimeFrame(), dl), tf, dl);
                    subTabbedPane.add("Chart " + dl, panel);
                }

                String avgName = "Average chart " + tf.getTimeFrame();
                subTabbedPane.add(avgName, createChartPanel(averageData.get(coin).get(tf), avgName, tf, DataLength.MAX_LENGTH));

                upperTabbedPane.add("Charts " + entry.getKey().getTimeFrame(), subTabbedPane);
            }
            chartTabbedPane.add(coin.toString(), upperTabbedPane);
        }
    }

    private ChartPanel createChartPanel(float[] predicts, String title, TimeFrame tf, DataLength dl) {
        DataObject[] origData = originalData.get(tf);
        OHLCDataset dataset = getDataSet(origData, dl.getCountInput());
        XYSeriesCollection lineDataset = getLineDataset(predicts, origData[origData.length - 1].getCandle().getOpenTime(), tf);

        DateAxis domainAxis = new DateAxis("Date");
        NumberAxis rangeAxis = new NumberAxis("Price");
        rangeAxis.setAutoRangeIncludesZero(false);

        CandlestickRenderer renderer = new CandlestickRenderer();
        XYPlot plot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        CandlestickRenderer candlestickRenderer = new CandlestickRenderer();
        plot.setRenderer(0, candlestickRenderer);

        plot.setDataset(1, lineDataset);

        XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer(true, true);

        float predMean = 0;
        for (float f : predicts) {
            predMean += f;
        }
        predMean /= predicts.length;
        Color predColor = predMean < origData[origData.length - 1].getCandle().getClose() ? Color.RED : Color.GREEN;

        lineRenderer.setSeriesPaint(0, predColor);
        plot.setRenderer(1, lineRenderer);

        JFreeChart chart = new JFreeChart(title, null, plot, true);
        return new ChartPanel(chart);
    }

    private OHLCDataset getDataSet(DataObject[] data, int size) {
        if (size == 0)
            size = data.length;

        Date[] date = new Date[size];
        double[] high = new double[size];
        double[] low = new double[size];
        double[] open = new double[size];
        double[] close = new double[size];
        double[] volume = new double[size];

        int index = 0;
        for (int i = data.length-size; i < data.length; i++) {
            CandleObject object = data[i].getCandle();
            date[index] = object.getOpenTime();
            high[index] = object.getHigh();
            low[index] = object.getLow();
            open[index] = object.getOpen();
            close[index] = object.getClose();
            volume[index] = 0;
            index++;
        }

        return new DefaultHighLowDataset("Candle chart", date, high, low, open, close, volume);
    }

    private XYSeriesCollection getLineDataset(float[] data, Date start, TimeFrame tf) {
        XYSeries series = new XYSeries("Predict chart");
        for (float f : data) {
            start = new Date(start.getTime() + ((long) tf.getMinuteCount() * 60 * 1000));
            series.add(start.getTime(), f);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        return dataset;
    }

    private void initGridPanel() {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));

        Font newButtonFont = new Font("Arial", Font.BOLD, 22);

        JButton refreshButton = new JButton("Refresh chart");
        refreshButton.setFont(newButtonFont);
        refreshButton.setBackground(Color.LIGHT_GRAY);
        refreshButton.addActionListener(e -> {
            new Thread(() -> {
                try {
                    refreshData();
                    refreshCharts();
                } catch (TranslateException ex) {
                    throw new RuntimeException(ex);
                }
            }).start();
        });

        JButton updateDataButton = new JButton("Update data");
        updateDataButton.setFont(newButtonFont);
        updateDataButton.setBackground(Color.LIGHT_GRAY);
        updateDataButton.addActionListener(e -> {
            new Thread(() -> {
                updater.updateData(fdUtil);
            }).start();
        });

        JButton reinitializeButton = new JButton("Reinit models");
        reinitializeButton.setFont(newButtonFont);
        reinitializeButton.setBackground(Color.LIGHT_GRAY);
        reinitializeButton.addActionListener(e -> {
            new Thread(() -> {
                for (Map.Entry<Coin, Initializer> entry : initializers.entrySet()) {
                    try {
                        entry.getValue().createAllModels();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }).start();
        });

        JButton updateModelsButton = new JButton("Update models");
        updateModelsButton.setFont(newButtonFont);
        updateModelsButton.setBackground(Color.LIGHT_GRAY);
        updateModelsButton.addActionListener(e -> {
            new Thread(() -> {
                for (Map.Entry<Coin, Initializer> entry : initializers.entrySet()) {
                    try {
                        entry.getValue().updateAllModels();
                    } catch (IOException | TranslateException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }).start();
        });

        JPanel gridPanel = new JPanel(new GridLayout(4, 1));
        gridPanel.add(refreshButton);
        gridPanel.add(updateDataButton);
        gridPanel.add(reinitializeButton);
        gridPanel.add(updateModelsButton);

        sidePanel.add(gridPanel);
        sidePanel.setBackground(Color.LIGHT_GRAY);

        add(sidePanel, BorderLayout.EAST);
    }

}
