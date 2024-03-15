package com.crypto.analysis.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FundingDataSetCreator {
    public static void main(String[] args) throws IOException, ParseException {
        List<String> candleLines = Files.readAllLines(Path.of("C:\\Users\\keklo\\OneDrive\\Рабочий стол\\Новая папка\\ll\\8h-candles-2022_06-to-2024_02.csv"));
        List<String> fundList = Files.readAllLines(Path.of("C:\\Users\\keklo\\OneDrive\\Рабочий стол\\Новая папка\\ll\\funding-2022_06-to-2024_02.csv"));
        candleLines.remove(0);
        fundList.remove(0);

        HashMap<Date, String> candles = new HashMap<>();
        for (String str : candleLines) {
            String[] tokens = str.split(",");
            Date openTime = new Date(Long.parseLong(tokens[0]));
            String open = tokens[1];
            String volume = tokens[5];
            candles.put(openTime, open+'='+volume);
        }

        TreeMap<Date, Double> fundingMap = new TreeMap<>();
        for (String str : fundList) {
            String[] tokens =str.split(",");
            StringBuilder builder = new StringBuilder(tokens[0]);
            builder.setCharAt(builder.length() -1, '0');
            builder.setCharAt(builder.length() -2, '0');
            Date date = new Date(Long.parseLong(builder.toString()));
            fundingMap.put(date, Double.parseDouble(tokens[2]));
        }

        PrintWriter writer = new PrintWriter("C:\\Users\\keklo\\OneDrive\\Рабочий стол\\Новая папка\\ll\\funding_set_multiple.csv");
        writer.println("timestamp,funding,price,volume");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC+0"));

        for (Map.Entry<Date, Double> entry : fundingMap.entrySet()) {
            Date current = entry.getKey();
            double volume = entry.getValue();
            String[] tokens = candles.get(current).split("=");
            writer.println(String.format("%s,%s,%s,%s", current.getTime(),volume, tokens[0], tokens[1]));
            writer.flush();
        }

    }

    public static Date addHoursToJavaUtilDate(Date date, int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        return calendar.getTime();
    }

}
