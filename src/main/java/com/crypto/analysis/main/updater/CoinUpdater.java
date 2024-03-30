package com.crypto.analysis.main.updater;

import com.crypto.analysis.main.data_utils.select.coin.Coin;
import com.crypto.analysis.main.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.data_utils.utils.binance.BinanceDataUtil;
import com.crypto.analysis.main.fundamental.crypto.BitQueryUtil;
import com.crypto.analysis.main.vo.indication.BuySellRatioHistoryObject;
import com.crypto.analysis.main.vo.indication.FundingHistoryObject;
import com.crypto.analysis.main.vo.indication.LongShortRatioHistoryObject;
import com.crypto.analysis.main.vo.indication.OpenInterestHistoryObject;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.crypto.analysis.main.data_utils.select.StaticData.*;

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
            if (lines.get(0).matches("[a-zA-Z]")) lines.remove(0);

            dates = new LinkedList<>();

            for (String line : lines) {
                dates.add(sdfFullISO.parse(line.split(",")[0]));
            }

            Date start = dates.getLast();
            bqUtil.initData(start, new Date());

            if (!dates.contains(bqUtil.getData().firstKey())) throw new IllegalStateException("Data list is not full");
            bqUtil.getData().remove(bqUtil.getData().lastKey());

            for (Map.Entry<Date, double[]> entry : bqUtil.getData().entrySet()) {
                writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",
                        sdfFullISO.format(entry.getKey()), entry.getValue()[0],
                        entry.getValue()[1], entry.getValue()[2], entry.getValue()[3],
                        entry.getValue()[4], entry.getValue()[5], entry.getValue()[6],
                        entry.getValue()[7]));
            }

            System.out.printf("Fundamental data updated at path %s%n", path.getFileName());
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
            if (lines.get(0).matches("[a-zA-Z]")) lines.remove(0);

            dates = new LinkedList<>();

            for (String line : lines) {
                dates.add(new Date(Long.parseLong(line.split(",")[0])));
            }

            FundingHistoryObject fundingObject = BinanceDataUtil.getFundingHistory(coin);

            if (!dates.contains(fundingObject.getMap().firstKey()))
                throw new IllegalStateException("Data list is not full");

            for (Map.Entry<Date, Double> entry : fundingObject.getMap().entrySet()) {
                if (!dates.contains(entry.getKey()))
                    writer.println(String.format("%s,8,%s", sdfFullISO.format(entry.getKey()), entry.getValue()));
            }
            System.out.printf("Fundamental crypto data updated at path %s%n", path.getFileName());
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
            if (lines.get(0).matches("[a-zA-Z]")) lines.remove(0);

            dates = new LinkedList<>();

            for (String line : lines) {
                dates.add(sdfFullISO.parse(line.split(",")[0]));
            }

            OpenInterestHistoryObject openInterestHistoryObject = BinanceDataUtil.getOpenInterest(coin, TimeFrame.FIVE_MINUTES);
            LongShortRatioHistoryObject longShortObject = BinanceDataUtil.getLongShortRatio(coin, TimeFrame.FIVE_MINUTES);
            BuySellRatioHistoryObject buySellRatioHistoryObject = BinanceDataUtil.getBuySellRatio(coin, TimeFrame.FIVE_MINUTES);

            if (!dates.contains(longShortObject.getMap().firstKey()) ||
                    !dates.contains(openInterestHistoryObject.getMap().firstKey()) ||
                    !dates.contains(buySellRatioHistoryObject.getMap().firstKey()))
                throw new IllegalStateException("Data list is not full");

            TreeMap<Date, double[]> dataMap = new TreeMap<>();

            for (Map.Entry<Date, Double> entry : openInterestHistoryObject.getMap().entrySet()) {
                dataMap.put(entry.getKey(), new double[]{entry.getValue(), 0, 0});
            }
            for (Map.Entry<Date, Double> entry : longShortObject.getMap().entrySet()) {
                if (!dataMap.containsKey(entry.getKey())) continue;
                dataMap.get(entry.getKey())[1] = entry.getValue();
            }
            for (Map.Entry<Date, Double> entry : buySellRatioHistoryObject.getMap().entrySet()) {
                if (!dataMap.containsKey(entry.getKey())) continue;
                dataMap.get(entry.getKey())[2] = entry.getValue();
            }

            if (!dates.contains(dataMap.firstKey())) throw new IllegalStateException("Data list is not full");

            for (Map.Entry<Date, double[]> entry : dataMap.entrySet()) {
                if (!dates.contains(entry.getKey())) writer.println(String.format("%s,%s,%s,0,0,0,%s,%s",
                        sdfFullISO.format(entry.getKey()), coin.getName(),
                        entry.getValue()[0], entry.getValue()[1], entry.getValue()[2]));
            }

            System.out.printf("Metrics data updated at path %s%n", path.getFileName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
