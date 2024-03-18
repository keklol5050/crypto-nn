package com.crypto.analysis.main;

import com.crypto.analysis.main.funding.FundingHistoryObject;
import com.crypto.analysis.main.vo.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DataSetCreator {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC+0"));
    }
    public static void main(String[] args) throws IOException, ParseException {
        List<String> candleLines = Files.readAllLines(Path.of("C:\\Users\\keklo\\OneDrive\\Рабочий стол\\Новая папка\\ll\\15m-candles-2022_06-to-2024_02.csv"));
        List<String> fundList = Files.readAllLines(Path.of("C:\\Users\\keklo\\OneDrive\\Рабочий стол\\Новая папка\\ll\\funding-2022_06-to-2024_02.csv"));
        List<String> metrics = Files.readAllLines(Path.of("C:\\Users\\keklo\\OneDrive\\Рабочий стол\\Новая папка\\ll\\metrics-2022_06-to-2024_02.csv"));
        List<String> dom = Files.readAllLines(Path.of("C:\\Users\\keklo\\OneDrive\\Рабочий стол\\Новая папка\\ll\\btcdom-15m-2022_06-to-2024_02.csv"));
        List<String> spx = Files.readAllLines(Path.of("C:\\Users\\keklo\\OneDrive\\Рабочий стол\\Новая папка\\ll\\spx-2022_06-to-2024_02.csv"));
        List<String> dxy = Files.readAllLines(Path.of("C:\\Users\\keklo\\OneDrive\\Рабочий стол\\Новая папка\\ll\\dxy-2022_06-to-2024_02.csv"));
        List<String> dji = Files.readAllLines(Path.of("C:\\Users\\keklo\\OneDrive\\Рабочий стол\\Новая папка\\ll\\dji-2022_06-to-2024_02.csv"));
        List<String> vix = Files.readAllLines(Path.of("C:\\Users\\keklo\\OneDrive\\Рабочий стол\\Новая папка\\ll\\vix-2022_06-to-2024_02.csv"));
        List<String> ndx = Files.readAllLines(Path.of("C:\\Users\\keklo\\OneDrive\\Рабочий стол\\Новая папка\\ll\\ndx-2022_06-to-2024_02.csv"));
        List<String> xau = Files.readAllLines(Path.of("C:\\Users\\keklo\\OneDrive\\Рабочий стол\\Новая папка\\ll\\xau-2022_06-to-2024_02.csv"));
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

        TreeMap<Date, Double> fundingMap = new TreeMap<Date, Double>();
        for (String str : fundList) {
            String[] tokens =str.split(",");
            fundingMap.put(new Date(Long.parseLong(tokens[0])), Double.parseDouble(tokens[2]));
        }
        FundingHistoryObject funding = new FundingHistoryObject(fundingMap);

        TreeMap<Date, Double> longShortMap = new TreeMap<Date, Double>();
        TreeMap<Date, Double> oiMap = new TreeMap<Date,Double>();
        TreeMap<Date, Double> bsrMap = new TreeMap<Date, Double>();

        for (String str : metrics) {
            String[] tokens = (str.split(","));
            Date date = sdf.parse(tokens[0]);
            double longShort = Double.parseDouble(tokens[6]);
            double oi = Double.parseDouble(tokens[2]);
            double bsr = Double.parseDouble(tokens[7]);
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
            Date openTime =sdf.parse(tokens[0]);
            double open = Double.parseDouble(tokens[1]);
            spxMap.put(openTime, open);
        }

        TreeMap<Date, Double> dxyMap = new TreeMap<Date, Double>();
        for (String str : dxy) {
            String[] tokens = str.split(";");
            Date openTime = sdf.parse(tokens[0]);
            double open = Double.parseDouble(tokens[1]);
            dxyMap.put(openTime, open);
        }

        TreeMap<Date, Double> djiMap = new TreeMap<Date, Double>();
        for (String str : dji) {
            String[] tokens = str.split(";");
            Date openTime = sdf.parse(tokens[0]);
            double open = Double.parseDouble(tokens[1]);
            djiMap.put(openTime, open);
        }


        TreeMap<Date, Double> vixMap = new TreeMap<Date, Double>();
        for (String str : vix) {
            String[] tokens = str.split(";");
            Date openTime = sdf.parse(tokens[0]);
            double open = Double.parseDouble(tokens[1]);
            vixMap.put(openTime, open);
        }

        TreeMap<Date, Double> ndxMap = new TreeMap<Date, Double>();
        for (String str : ndx) {
            String[] tokens = str.split(";");
            Date openTime = sdf.parse(tokens[0]);
            double open = Double.parseDouble(tokens[1]);
            ndxMap.put(openTime, open);
        }

        TreeMap<Date, Double> xauMap = new TreeMap<Date, Double>();
        for (String str : xau) {
            String[] tokens = str.split(";");
            Date openTime = sdf.parse(tokens[0]);
            double open = Double.parseDouble(tokens[1]);
            xauMap.put(openTime, open);
        }


        PrintWriter writer = new PrintWriter("C:\\Users\\keklo\\OneDrive\\Рабочий стол\\Новая папка\\ll\\bitcoin_15m.csv");
        writer.println("open_time,open,high,low,close,volume,close_time,funding,open_interest,long_short_ratio,taker_buy_sell_ratio,btc_dom,spx,dxy,dji,vix,ndx,gold");


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
            String result = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", sdf.format(current), candle.getOpen(), candle.getHigh(),
                    candle.getLow(), candle.getClose(), candle.getVolume(), sdf.format(candle.getCloseTime()),fund, oi, ls, bsr, btcDOM,
                    spxV, dxyV, djiV, vixV, ndxV, xauV);
            writer.println(result);
            writer.flush();
        }
    }
}
