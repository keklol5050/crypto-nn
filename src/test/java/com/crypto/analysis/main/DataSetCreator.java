package com.crypto.analysis.main;

import com.crypto.analysis.main.vo.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DataSetCreator {
    public static void main(String[] args) throws IOException, ParseException {
        List<String> candleLines = Files.readAllLines(Path.of("C:\\Users\\keklo\\OneDrive\\Рабочий стол\\Новая папка\\ll\\15m-candles-2022_06-to-2024_02.csv"));
        List<String> fundList = Files.readAllLines(Path.of("C:\\Users\\keklo\\OneDrive\\Рабочий стол\\Новая папка\\ll\\funding-2022_06-to-2024_02.csv"));
        List<String> metrics = Files.readAllLines(Path.of("C:\\Users\\keklo\\OneDrive\\Рабочий стол\\Новая папка\\ll\\metrics-2022_06-to-2024_02.csv"));
        candleLines.remove(0);
        fundList.remove(0);
        metrics.remove(0);

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

        TreeMap<String, Double> longShortMap = new TreeMap<String, Double>();
        TreeMap<String, Double> oiMap = new TreeMap<String,Double>();
        TreeMap<String, Double> bsrMap = new TreeMap<String, Double>();

        for (String str : metrics) {
            String[] tokens = (str.split(","));
            double longShort = Double.parseDouble(tokens[6]);
            double oi = Double.parseDouble(tokens[2]);
            double bsr = Double.parseDouble(tokens[7]);
            longShortMap.put(tokens[0], longShort);
            oiMap.put(tokens[0], oi);
            bsrMap.put(tokens[0], bsr);
        }

        PrintWriter writer = new PrintWriter("C:\\Users\\keklo\\OneDrive\\Рабочий стол\\Новая папка\\ll\\bitcoin_15m.csv");
        writer.println("open_time,open,high,low,close,volume,close_time,funding,open_interest,long_short_ratio,taker_buy_sell_ratio");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC+0"));

        for (CandleObject candle : candles) {
            Date current = candle.getOpenTime();
            String date = sdf.format(current);
            double fund = funding.getValueForNearestDate(current);
            double oi = oiMap.get(date);
            double ls = longShortMap.get(date);
            double bsr = bsrMap.get(date);
            String result = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", date, candle.getOpen(), candle.getHigh(),
                    candle.getLow(), candle.getClose(), candle.getVolume(), sdf.format(candle.getCloseTime()),fund, oi, ls, bsr);
            writer.println(result);
            writer.flush();
        }
    }
}
