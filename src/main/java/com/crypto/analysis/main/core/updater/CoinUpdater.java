package com.crypto.analysis.main.core.updater;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.BinanceDataUtil;
import com.crypto.analysis.main.core.data_utils.utils.PropertiesUtil;
import com.crypto.analysis.main.core.fundamental.crypto.BitQueryUtil;
import com.crypto.analysis.main.core.vo.indication.FundingHistoryObject;
import com.crypto.analysis.main.core.vo.indication.LongShortRatioHistoryObject;
import com.crypto.analysis.main.core.vo.indication.OpenInterestHistoryObject;
import kotlin.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;

import static com.crypto.analysis.main.core.data_utils.select.StaticUtils.sdfFullISO;

public class CoinUpdater {
    private final Coin coin;

    private BitQueryUtil bqUtil;
    private List<String> lines;
    private ArrayList<Date> dates;

    private static final Logger logger = LoggerFactory.getLogger(CoinUpdater.class);

    public CoinUpdater(Coin coin) {
        this.coin = coin;
        logger.info("Initializing updater for coin {}", coin);
    }

    public void updateFundamentalData(TimeFrame tf) {
        Path path = Path.of(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getParent() + PropertiesUtil.getProperty("data.data_path") + coin + "/fund/" + tf.getTimeFrame() + ".csv");
        bqUtil = new BitQueryUtil(coin, tf);

        try (PrintWriter writer = new PrintWriter(new FileWriter(String.valueOf(path), true))) {

            lines = Files.readAllLines(path);
            lines.removeFirst();

            dates = new ArrayList<>();

            for (String line : lines) {
                dates.add(sdfFullISO.parse(line.split(",")[0]));
            }

            Date start = dates.getLast();
            bqUtil.initData(start, new Date());

            if (!dates.contains(bqUtil.getData().firstKey())) throw new IllegalStateException("Data list is not full");
            bqUtil.getData().remove(bqUtil.getData().lastKey());

            for (Map.Entry<Date, float[]> entry : bqUtil.getData().entrySet()) {
                if (entry.getKey().after(dates.getLast()) && !dates.contains(entry.getKey()))
                    writer.print('\n' + sdfFullISO.format(entry.getKey()) + ',' + Arrays.toString(entry.getValue()).replaceAll("[\\[\\] ]", ""));
            }

            logger.info("Fundamental data of {} updated at path {}", coin, path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void updateFunding() {
        Path path = Path.of(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getParent() + PropertiesUtil.getProperty("data.data_path") + coin + "/funding.csv");

        try (PrintWriter writer = new PrintWriter(new FileWriter(String.valueOf(path), true))) {

            lines = Files.readAllLines(path);
            lines.removeFirst();

            dates = new ArrayList<>();

            for (String line : lines) {
                dates.add(sdfFullISO.parse(line.split(",")[0]));
            }

            FundingHistoryObject fundingObject = BinanceDataUtil.getFundingHistory(coin);

            StringBuilder builderKey = new StringBuilder(String.valueOf(fundingObject.getMap().firstKey().getTime()));
            builderKey.setCharAt(builderKey.length() - 1, '0');
            builderKey.setCharAt(builderKey.length() - 2, '0');
            builderKey.setCharAt(builderKey.length() - 3, '0');
            builderKey.setCharAt(builderKey.length() - 4, '0');
            Date key = new Date(Long.parseLong(builderKey.toString()));

            if (!dates.contains(key))
                throw new IllegalStateException("Data list is not full");

            for (Map.Entry<Date, Float> entry : fundingObject.getMap().entrySet()) {
                StringBuilder builder = new StringBuilder(String.valueOf(entry.getKey().getTime()));
                builder.setCharAt(builder.length() - 1, '0');
                builder.setCharAt(builder.length() - 2, '0');
                builder.setCharAt(builder.length() - 3, '0');
                builder.setCharAt(builder.length() - 4, '0');
                Date date = new Date(Long.parseLong(builder.toString()));

                if (date.after(dates.getLast()) && !dates.contains(date)) {
                    writer.print(String.format("\n%s,%f", sdfFullISO.format(date), entry.getValue()));
                }
            }
            logger.info("Funding data of {} updated at path {}", coin, path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateMetrics() {
        Path path = Path.of(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getParent() + PropertiesUtil.getProperty("data.data_path") + coin + "/metrics.csv");

        try (PrintWriter writer = new PrintWriter(new FileWriter(String.valueOf(path), true))) {

            lines = Files.readAllLines(path);
            lines.removeFirst();

            dates = new ArrayList<>();

            for (String line : lines) {
                dates.add(sdfFullISO.parse(line.split(",")[0]));
            }

            OpenInterestHistoryObject openInterestHistoryObject = BinanceDataUtil.getOpenInterest(coin, TimeFrame.FIFTEEN_MINUTES);
            LongShortRatioHistoryObject longShortObject = BinanceDataUtil.getLongShortRatio(coin, TimeFrame.FIFTEEN_MINUTES);
            TreeMap<Date, double[]> dataMap = new TreeMap<>();

            StringBuilder builderKey = new StringBuilder(String.valueOf(openInterestHistoryObject.getMap().firstKey().getTime()));
            builderKey.setCharAt(builderKey.length() - 1, '0');
            builderKey.setCharAt(builderKey.length() - 2, '0');
            builderKey.setCharAt(builderKey.length() - 3, '0');
            builderKey.setCharAt(builderKey.length() - 4, '0');
            Date key = new Date(Long.parseLong(builderKey.toString()));
            if (!dates.contains(key))
                throw new IllegalStateException("Data list is not full");

            builderKey = new StringBuilder(String.valueOf(longShortObject.getMap().firstKey().getTime()));
            builderKey.setCharAt(builderKey.length() - 1, '0');
            builderKey.setCharAt(builderKey.length() - 2, '0');
            builderKey.setCharAt(builderKey.length() - 3, '0');
            builderKey.setCharAt(builderKey.length() - 4, '0');
            key = new Date(Long.parseLong(builderKey.toString()));
            if (!dates.contains(key))
                throw new IllegalStateException("Data list is not full");


            for (Map.Entry<Date, Float> entry : openInterestHistoryObject.getMap().entrySet()) {
                dataMap.put(entry.getKey(), new double[]{entry.getValue(), 0});
            }
            for (Map.Entry<Date, Float> entry : longShortObject.getMap().entrySet()) {
                if (!dataMap.containsKey(entry.getKey())) continue;
                dataMap.get(entry.getKey())[1] = entry.getValue();
            }

            dataMap.pollLastEntry();

            for (Map.Entry<Date, double[]> entry : dataMap.entrySet()) {
                StringBuilder builder = new StringBuilder(String.valueOf(entry.getKey().getTime()));
                builder.setCharAt(builder.length() - 1, '0');
                builder.setCharAt(builder.length() - 2, '0');
                builder.setCharAt(builder.length() - 3, '0');
                builder.setCharAt(builder.length() - 4, '0');
                Date date = new Date(Long.parseLong(builder.toString()));

                if (date.after(dates.getLast()) && !dates.contains(date)) {
                    writer.print(String.format("\n%s,%s,%s", sdfFullISO.format(date), entry.getValue()[0], entry.getValue()[1]));
                }
            }

            logger.info("Metrics data of of {} updated at path {}", coin, path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static TreeMap<Date, Pair<Float, Float>> getMetricsMap(Coin coin) throws IOException { // {Open Interest, LongShort Ratio}
        TreeMap<Date, Pair<Float, Float>> result = new TreeMap<>();
        Path path = Path.of(new File(CoinUpdater.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParent() + PropertiesUtil.getProperty("data.data_path") + coin + "/metrics.csv");

        List<String> lines = Files.readAllLines(path);
        lines.removeFirst();

        for (String line : lines) {
            try {
                String[] split = line.split(",");
                result.put(sdfFullISO.parse(split[0]), new Pair<>(Float.parseFloat(split[1]), Float.parseFloat(split[2])));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        return result;
    }
}
