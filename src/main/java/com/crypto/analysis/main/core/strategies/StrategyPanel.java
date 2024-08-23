package com.crypto.analysis.main.core.strategies;

import com.crypto.analysis.main.core.data_utils.normalizers.Transposer;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.BinanceDataUtil;
import com.crypto.analysis.main.core.data_utils.utils.IndicatorsDataUtil;
import com.crypto.analysis.main.core.vo.CandleObject;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.TextAnchor;
import org.ta4j.core.*;
import org.ta4j.core.num.DecimalNum;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class StrategyPanel extends JPanel {
    private JTabbedPane chartTabbedPane = new JTabbedPane();
    private TreeMap<Coin,TreeMap<TimeFrame, BarSeries>> data;

    public StrategyPanel() {
        init();
    }

    private void init() {
        this.setLayout(new BorderLayout());
        add(chartTabbedPane, BorderLayout.CENTER);
        initGridPanel();
    }

    private void refreshData() {
        data = new TreeMap<>();
        for (Coin coin : Coin.values()) {
            if (coin == Coin.BTCDOMUSDT) continue;
            TreeMap<TimeFrame, BarSeries> currData = new TreeMap<>();

            for (TimeFrame tf : TimeFrame.values()) {
               ArrayList<CandleObject> candles = BinanceDataUtil.getCandles(coin, tf, 1500);
               currData.put(tf, IndicatorsDataUtil.getTimeSeries(candles));
            }
            data.put(coin, currData);
        }
    }

    public void refreshChart(StrategyType type) {
        chartTabbedPane.removeAll();
        for (Map.Entry<Coin,TreeMap<TimeFrame, BarSeries>> upperEntry : data.entrySet()){
            JTabbedPane upperTabbedPane = new JTabbedPane();

            for (Map.Entry<TimeFrame, BarSeries> entry : upperEntry.getValue().entrySet()) {
                BarSeries series = entry.getValue();
                BarSeriesManager manager = new BarSeriesManager(series);

                BaseStrategy strategy = StrategyInitializer.getStrategy(series, type);
                TradingRecord tradingRecord = manager.run(strategy, Trade.TradeType.BUY, DecimalNum.valueOf(1));

                List<Position> positions = tradingRecord.getPositions();
                ArrayList<Integer> longs  = new ArrayList<>();
                ArrayList<Integer> shorts = new ArrayList<>();

                for (Position p : positions) {
                    longs.add(p.getEntry().getIndex());
                    shorts.add(p.getExit().getIndex());
                }

                upperTabbedPane.add("Timeframe " + entry.getKey().getTimeFrame(), getChartPanel(series, longs, shorts));
            }
            chartTabbedPane.add(upperEntry.getKey().toString(), upperTabbedPane);
        }
    }
    private ChartPanel getChartPanel(BarSeries series, ArrayList<Integer> longIndices, ArrayList<Integer> shortIndices) {
        double[][] cData = new double[series.getBarCount()][];
        Date[] dates = new Date[series.getBarCount()];
        for (int i = 0; i < series.getBarCount(); i++) {
            Bar bar = series.getBar(i);
            dates[i] = Date.from(bar.getEndTime().toInstant());
            cData[i] = new double[5];
            cData[i][0] = bar.getOpenPrice().doubleValue();
            cData[i][1] = bar.getHighPrice().doubleValue();
            cData[i][2] = bar.getLowPrice().doubleValue();
            cData[i][3] = bar.getClosePrice().doubleValue();
            cData[i][4] = 0;
        }
        cData = Transposer.transpose(cData);

        XYDataset dataset = new DefaultHighLowDataset("Prediction", dates, cData[1], cData[2], cData[0], cData[3], cData[4]);
        CandlestickRenderer renderer = new CandlestickRenderer();

        String title = "Chart prediction";
        DateAxis domainAxis = new DateAxis("Date");
        NumberAxis rangeAxis = new NumberAxis("Price");
        rangeAxis.setAutoRangeIncludesZero(false);

        XYPlot plot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        for (Integer i : longIndices) {
            double x = dates[i].getTime();
            double y = series.getBar(i).getClosePrice().doubleValue();
            XYPointerAnnotation pointer = new XYPointerAnnotation("", x, y, Math.PI / 2);
            pointer.setBaseRadius(25.0);
            pointer.setTipRadius(25.0);
            pointer.setArrowWidth(6);
            pointer.setArrowLength(6);
            pointer.setFont(new Font("SansSerif", Font.PLAIN, 10));
            pointer.setPaint(Color.GREEN);
            pointer.setArrowPaint(Color.GREEN);
            pointer.setTextAnchor(TextAnchor.BOTTOM_CENTER);
            plot.addAnnotation(pointer);
        }

        for (Integer i : shortIndices) {
            double x = dates[i].getTime();
            double y = series.getBar(i).getClosePrice().doubleValue();
            XYPointerAnnotation pointer = new XYPointerAnnotation("", x, y, 3 * Math.PI / 2);
            pointer.setBaseRadius(25.0);
            pointer.setTipRadius(25.0);
            pointer.setArrowWidth(6);
            pointer.setArrowLength(6);
            pointer.setFont(new Font("SansSerif", Font.PLAIN, 10));
            pointer.setPaint(Color.RED);
            pointer.setArrowPaint(Color.RED);
            pointer.setTextAnchor(TextAnchor.TOP_CENTER);
            plot.addAnnotation(pointer);
        }

        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        return new ChartPanel(chart);
    }

    private void initGridPanel() {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));

        Font newButtonFont = new Font("Arial", Font.BOLD, 22);

        JButton rsiAndStochastic = new JButton("RSI&Stochastic");
        rsiAndStochastic.setFont(newButtonFont);
        rsiAndStochastic.setBackground(Color.LIGHT_GRAY);
        rsiAndStochastic.addActionListener(e -> {
            new Thread(() -> {
                    refreshData();
                    refreshChart(StrategyType.RSI_STOCHASTIC);
            }).start();
        });

        JButton rsiStrategy = new JButton("RSI strategy");
        rsiStrategy.setFont(newButtonFont);
        rsiStrategy.setBackground(Color.LIGHT_GRAY);
        rsiStrategy.addActionListener(e -> {
            new Thread(() -> {
                refreshData();
                refreshChart(StrategyType.RSI);
            }).start();
        });

        JButton adxStrategy = new JButton("ADX Strategy");
        adxStrategy.setFont(newButtonFont);
        adxStrategy.setBackground(Color.LIGHT_GRAY);
        adxStrategy.addActionListener(e -> {
            new Thread(() -> {
                refreshData();
                refreshChart(StrategyType.ADX);
            }).start();
        });

        JButton smaStrategy = new JButton("SMA Strategy");
        smaStrategy.setFont(newButtonFont);
        smaStrategy.setBackground(Color.LIGHT_GRAY);
        smaStrategy.addActionListener(e -> {
            new Thread(() -> {
                refreshData();
                refreshChart(StrategyType.SMA);
            }).start();
        });

        JPanel gridPanel = new JPanel(new GridLayout(4, 1));
        gridPanel.add(rsiAndStochastic);
        gridPanel.add(rsiStrategy);
        gridPanel.add(adxStrategy);
        gridPanel.add(smaStrategy);

        sidePanel.add(gridPanel);
        sidePanel.setBackground(Color.LIGHT_GRAY);

        add(sidePanel, BorderLayout.EAST);
    }

}
