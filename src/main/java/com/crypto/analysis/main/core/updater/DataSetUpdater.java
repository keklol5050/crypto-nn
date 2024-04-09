package com.crypto.analysis.main.core.updater;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.vo.CandleObject;
import com.crypto.analysis.main.core.vo.indication.FundingHistoryObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import static com.crypto.analysis.main.core.data_utils.select.StaticData.*;

public class DataSetUpdater {
    public void update(Coin coin, TimeFrame tf) throws IOException, ParseException {
        Path pathToCandles = switch (coin) {
            case BTCUSDT -> switch (tf) {
                case FIFTEEN_MINUTES -> pathToBTCCandles15m;
                case ONE_HOUR -> pathToBTCCandles1h;
                case FOUR_HOUR -> pathToBTCCandles4h;
            };
            default -> throw new IllegalStateException("Unexpected value: " + coin);
        };

        Path pathToFunding = switch (coin) {
            case BTCUSDT -> pathToBTCFunding;
            default -> throw new IllegalStateException("Unexpected value: " + coin);
        };

        Path pathToMetrics = switch (coin) {
            case BTCUSDT -> pathToBTCMetrics;
            default -> throw new IllegalStateException("Unexpected value: " + coin);
        };

        Path pathToFundValues = switch (coin) {
            case BTCUSDT -> switch (tf) {
                case FIFTEEN_MINUTES -> pathToBTCFund15m;
                case ONE_HOUR -> pathToBTCFund1h;
                case FOUR_HOUR -> pathToBTCFund4h;
            };
            default -> throw new IllegalStateException("Unexpected value: " + coin);
        };

        Path pathToDataSet = switch (coin) {
            case BTCUSDT -> switch (tf) {
                case FIFTEEN_MINUTES -> pathToFifteenMinutesBTCDataSet;
                case ONE_HOUR -> pathToOneHourBTCDataSet;
                case FOUR_HOUR -> pathToFourHourBTCDataSet;
            };
            default -> throw new IllegalStateException("Unexpected value: " + coin);
        };

        List<String> candleLines = Files.readAllLines(pathToCandles);
        List<String> fundList = Files.readAllLines(pathToFunding);
        List<String> metrics = Files.readAllLines(pathToMetrics);
        List<String> fundCr = Files.readAllLines(pathToFundValues);

        List<String> dom = Files.readAllLines(pathToBTCDOM);
        List<String> spx = Files.readAllLines(pathToSPX);
        List<String> dxy = Files.readAllLines(pathToDXY);
        List<String> dji = Files.readAllLines(pathToDJI);
        List<String> vix = Files.readAllLines(pathToVIX);
        List<String> ndx = Files.readAllLines(pathToNDX);
        List<String> xau = Files.readAllLines(pathToGOLD);

        candleLines.remove(0);
        fundList.remove(0);
        metrics.remove(0);
        dom.remove(0);
        spx.remove(0);
        dxy.remove(0);
        dji.remove(0);
        vix.remove(0);
        ndx.remove(0);
        xau.remove(0);
        fundCr.remove(0);

        LinkedList<CandleObject> candles = new LinkedList<CandleObject>();
        for (String str : candleLines) {
            String[] tokens = str.split(",");
            Date openTime = new Date(Long.parseLong(tokens[0]));
            double open = Double.parseDouble(tokens[1]);
            double high = Double.parseDouble(tokens[2]);
            double low = Double.parseDouble(tokens[3]);
            double close = Double.parseDouble(tokens[4]);
            double volume = Double.parseDouble(tokens[5]);
            Date closeTime = new Date(Long.parseLong(tokens[6]));
            candles.add(new CandleObject(openTime, open, high, low, close, volume, closeTime));
        }

        TreeMap<Date, Double> fundingMap = new TreeMap<>();
        for (String str : fundList) {
            String[] tokens = str.split(",");
            fundingMap.put(new Date(Long.parseLong(tokens[0])), Double.parseDouble(tokens[2]));
        }
        FundingHistoryObject funding = new FundingHistoryObject(fundingMap);

        TreeMap<Date, Double> longShortMap = new TreeMap<Date, Double>();
        TreeMap<Date, Double> oiMap = new TreeMap<Date, Double>();
        TreeMap<Date, Double> bsrMap = new TreeMap<Date, Double>();

        for (String str : metrics) {
            String[] tokens = (str.split(","));
            Date date = sdfFullISO.parse(tokens[0]);
            double longShort = Double.parseDouble(tokens[6]);
            double oi = Double.parseDouble(tokens[2]);
            double bsr = Double.parseDouble(tokens[7]);
            if (longShort == 0 || oi == 0 || bsr == 0) throw new RuntimeException();
            longShortMap.put(date, longShort);
            oiMap.put(date, oi);
            bsrMap.put(date, bsr);
        }

        TreeMap<Date, Double> domMap = new TreeMap<Date, Double>();
        for (String str : dom) {
            String[] tokens = str.split(",");
            Date openTime = new Date(Long.parseLong(tokens[0]));
            double open = Double.parseDouble(tokens[1]);
            domMap.put(openTime, open);
        }

        TreeMap<Date, Double> spxMap = new TreeMap<Date, Double>();
        for (String str : spx) {
            String[] tokens = str.split(";");
            Date openTime = sdfFullISO.parse(tokens[0]);
            double open = Double.parseDouble(tokens[1]);
            spxMap.put(openTime, open);
        }

        TreeMap<Date, Double> dxyMap = new TreeMap<Date, Double>();
        for (String str : dxy) {
            String[] tokens = str.split(";");
            Date openTime = sdfFullISO.parse(tokens[0]);
            double open = Double.parseDouble(tokens[1]);
            dxyMap.put(openTime, open);
        }

        TreeMap<Date, Double> djiMap = new TreeMap<Date, Double>();
        for (String str : dji) {
            String[] tokens = str.split(";");
            Date openTime = sdfFullISO.parse(tokens[0]);
            double open = Double.parseDouble(tokens[1]);
            djiMap.put(openTime, open);
        }


        TreeMap<Date, Double> vixMap = new TreeMap<Date, Double>();
        for (String str : vix) {
            String[] tokens = str.split(";");
            Date openTime = sdfFullISO.parse(tokens[0]);
            double open = Double.parseDouble(tokens[1]);
            vixMap.put(openTime, open);
        }

        TreeMap<Date, Double> ndxMap = new TreeMap<Date, Double>();
        for (String str : ndx) {
            String[] tokens = str.split(";");
            Date openTime = sdfFullISO.parse(tokens[0]);
            double open = Double.parseDouble(tokens[1]);
            ndxMap.put(openTime, open);
        }

        TreeMap<Date, Double> xauMap = new TreeMap<Date, Double>();
        for (String str : xau) {
            String[] tokens = str.split(";");
            Date openTime = sdfFullISO.parse(tokens[0]);
            double open = Double.parseDouble(tokens[1]);
            xauMap.put(openTime, open);
        }

        TreeMap<Date, double[]> fundCrMap = new TreeMap<Date, double[]>();
        for (String str : fundCr) {
            String[] tokens = str.split(",");
            Date openTime = sdfFullISO.parse(tokens[0]);
            fundCrMap.put(openTime, new double[]{
                    Double.parseDouble(tokens[1]),
                    Double.parseDouble(tokens[2]),
                    Double.parseDouble(tokens[3]),
                    Double.parseDouble(tokens[4]),
                    Double.parseDouble(tokens[5]),
                    Double.parseDouble(tokens[6]),
                    Double.parseDouble(tokens[7]),
                    Double.parseDouble(tokens[8])
            });
        }

        try (PrintWriter writer = new PrintWriter(String.valueOf(pathToDataSet))) {
            writer.println("open_time,open,high,low,close,volume,close_time,funding,open_interest,long_short_ratio,taker_buy_sell_ratio,btc_dom,spx,dxy,dji,vix,ndx,gold,transactions_count,fee_value,fee_average,input_count,input_value,mined_value,output_count,output_value");

            for (CandleObject candle : candles) {
                Date current = candle.getOpenTime();
                double fund = funding.getValueForNearestDate(current);
                double oi = oiMap.get(current);
                double ls = longShortMap.get(current);
                double bsr = bsrMap.get(current);
                double btcDOM = domMap.get(current);
                double spxV = spxMap.floorEntry(current).getValue();
                double dxyV = dxyMap.floorEntry(current).getValue();
                double djiV = djiMap.floorEntry(current).getValue();
                double vixV = vixMap.floorEntry(current).getValue();
                double ndxV = ndxMap.floorEntry(current).getValue();
                double xauV = xauMap.floorEntry(current).getValue();
                double[] trans = fundCrMap.floorEntry(current).getValue();
                String result = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", sdfFullISO.format(current), candle.getOpen(), candle.getHigh(),
                        candle.getLow(), candle.getClose(), candle.getVolume(), sdfFullISO.format(candle.getCloseTime()), fund, oi, ls, bsr, btcDOM,
                        spxV, dxyV, djiV, vixV, ndxV, xauV, trans[0], trans[1], trans[2], trans[3], trans[4], trans[5], trans[6], trans[7]);
                writer.println(result);
                writer.flush();
            }
        }

        System.out.println("Updated "+ coin + " data set at path " + pathToDataSet);
    }
}
