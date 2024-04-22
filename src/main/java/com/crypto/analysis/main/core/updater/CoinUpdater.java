package com.crypto.analysis.main.core.updater;

import com.crypto.analysis.main.core.vo.indication.FundingHistoryObject;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.binance.BinanceDataUtil;
import com.crypto.analysis.main.core.fundamental.crypto.BitQueryUtil;
import com.crypto.analysis.main.core.vo.indication.LongShortRatioHistoryObject;
import com.crypto.analysis.main.core.vo.indication.OpenInterestHistoryObject;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.crypto.analysis.main.core.data_utils.select.StaticData.*;

public class CoinUpdater {
    private final Coin coin;

    private BitQueryUtil bqUtil;
    private List<String> lines;
    private LinkedList<Date> dates;

    public CoinUpdater(Coin coin) {
        this.coin = coin;
    }

    public void updateFundamentalData(TimeFrame tf) {
        Path path = switch (coin) {
            case BTCUSDT -> switch (tf) {
                case FIFTEEN_MINUTES -> pathToBTCFund15m;
                case ONE_HOUR -> pathToBTCFund1h;
                case FOUR_HOUR -> pathToBTCFund4h;
                default -> throw new RuntimeException("Invalid time frame");
            };
            default -> throw new IllegalArgumentException();
        };

        bqUtil = new BitQueryUtil(coin, tf);

        try (PrintWriter writer = new PrintWriter(new FileWriter(String.valueOf(path), true))) {

            lines = Files.readAllLines(path);
            lines.remove(0);

            dates = new LinkedList<>();

            for (String line : lines) {
                dates.add(sdfFullISO.parse(line.split(",")[0]));
            }

            Date start = dates.getLast();
            bqUtil.initData(start, new Date());

            if (!dates.contains(bqUtil.getData().firstKey())) throw new IllegalStateException("Data list is not full");
            bqUtil.getData().remove(bqUtil.getData().lastKey());

            for (Map.Entry<Date, double[]> entry : bqUtil.getData().entrySet()) {
               if (entry.getKey().after(dates.getLast()) && !dates.contains(entry.getKey())) writer.print(String.format("\n%s,%s,%s,%s,%s,%s,%s,%s,%s",
                       sdfFullISO.format(entry.getKey()), entry.getValue()[0],
                       entry.getValue()[1], entry.getValue()[2], entry.getValue()[3],
                       entry.getValue()[4], entry.getValue()[5], entry.getValue()[6],
                       entry.getValue()[7]));
            }

            System.out.printf("Fundamental data of " + coin + " updated at path %s%n", path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void updateFunding() {
        Path path = switch (coin) {
            case BTCUSDT -> pathToBTCFunding;
            default -> throw new IllegalArgumentException();
        };

        try (PrintWriter writer = new PrintWriter(new FileWriter(String.valueOf(path), true))) {

            lines = Files.readAllLines(path);
            lines.remove(0);

            dates = new LinkedList<>();

            for (String line : lines) {
                dates.add(new Date(Long.parseLong(line.split(",")[0])));
            }

            FundingHistoryObject fundingObject = BinanceDataUtil.getFundingHistory(coin);

            StringBuilder builderKey = new StringBuilder(String.valueOf(fundingObject.getMap().firstKey().getTime()));
            builderKey.setCharAt(builderKey.length() -1, '0');
            builderKey.setCharAt(builderKey.length() -2, '0');
            builderKey.setCharAt(builderKey.length() -3, '0');
            builderKey.setCharAt(builderKey.length() -4, '0');
            Date key = new Date(Long.parseLong(builderKey.toString()));

            if (!dates.contains(key))
                throw new IllegalStateException("Data list is not full");

            for (Map.Entry<Date, Double> entry : fundingObject.getMap().entrySet()) {
                StringBuilder builder = new StringBuilder(String.valueOf(entry.getKey().getTime()));
                builder.setCharAt(builder.length() -1, '0');
                builder.setCharAt(builder.length() -2, '0');
                builder.setCharAt(builder.length() -3, '0');
                builder.setCharAt(builder.length() -4, '0');
                Date date = new Date(Long.parseLong(builder.toString()));

                if (date.after(dates.getLast()) && !dates.contains(date)){
                    writer.print(String.format("\n%s,8,%.8f", date.getTime(), entry.getValue()));
                }
            }
            System.out.printf("Funding data of " + coin + " updated at path %s%n", path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateMetrics() {
        Path path = switch (coin) {
            case BTCUSDT -> pathToBTCMetrics;
            default -> throw new IllegalArgumentException();
        };

        try (PrintWriter writer = new PrintWriter(new FileWriter(String.valueOf(path), true))) {

            lines = Files.readAllLines(path);
            lines.remove(0);

            dates = new LinkedList<>();

            for (String line : lines) {
                dates.add(sdfFullISO.parse(line.split(",")[0]));
            }

            OpenInterestHistoryObject openInterestHistoryObject = BinanceDataUtil.getOpenInterest(coin, TimeFrame.FIFTEEN_MINUTES);
            LongShortRatioHistoryObject longShortObject = BinanceDataUtil.getLongShortRatio(coin, TimeFrame.FIFTEEN_MINUTES);
            TreeMap<Date, double[]> dataMap = new TreeMap<>();

            StringBuilder builderKey = new StringBuilder(String.valueOf(openInterestHistoryObject.getMap().firstKey().getTime()));
            builderKey.setCharAt(builderKey.length() -1, '0');
            builderKey.setCharAt(builderKey.length() -2, '0');
            builderKey.setCharAt(builderKey.length() -3, '0');
            builderKey.setCharAt(builderKey.length() -4, '0');
            Date key = new Date(Long.parseLong(builderKey.toString()));
            if (!dates.contains(key))
                throw new IllegalStateException("Data list is not full");

            builderKey = new StringBuilder(String.valueOf(longShortObject.getMap().firstKey().getTime()));
            builderKey.setCharAt(builderKey.length() -1, '0');
            builderKey.setCharAt(builderKey.length() -2, '0');
            builderKey.setCharAt(builderKey.length() -3, '0');
            builderKey.setCharAt(builderKey.length() -4, '0');
            key = new Date(Long.parseLong(builderKey.toString()));
            if (!dates.contains(key))
                throw new IllegalStateException("Data list is not full");


            for (Map.Entry<Date, Double> entry : openInterestHistoryObject.getMap().entrySet()) {
                dataMap.put(entry.getKey(), new double[]{entry.getValue(), 0});
            }
            for (Map.Entry<Date, Double> entry : longShortObject.getMap().entrySet()) {
                if (!dataMap.containsKey(entry.getKey())) continue;
                dataMap.get(entry.getKey())[1] = entry.getValue();
            }

            dataMap.pollLastEntry();

            for (Map.Entry<Date, double[]> entry : dataMap.entrySet()) {
                StringBuilder builder = new StringBuilder(String.valueOf(entry.getKey().getTime()));
                builder.setCharAt(builder.length() -1, '0');
                builder.setCharAt(builder.length() -2, '0');
                builder.setCharAt(builder.length() -3, '0');
                builder.setCharAt(builder.length() -4, '0');
                Date date = new Date(Long.parseLong(builder.toString()));

                if (date.after(dates.getLast()) && !dates.contains(date)){
                    writer.print(String.format("\n%s,%s,%s,0,0,0,%s,0",
                            sdfFullISO.format(date), coin.getName(),
                            entry.getValue()[0], entry.getValue()[1]));
                }
            }

            System.out.printf("Metrics data of " + coin + " updated at path %s%n", path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
