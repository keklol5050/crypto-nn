package com.crypto.analysis.main.ndata;

import com.crypto.analysis.main.vo.CandleObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CSVHourAndDayTF {
    private static final Path sourceHour = new File(CSVHourAndDayTF.class.getClassLoader().getResource("static/bitcoin_1h.txt").getFile()).toPath();
    private static final Path sourceDay = new File(CSVHourAndDayTF.class.getClassLoader().getResource("static/bitcoin_1d.txt").getFile()).toPath();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private static LinkedList<CandleObject> readCandles(List<String> lines) {
        LinkedList<CandleObject> result = new LinkedList<CandleObject>();
        for (String line : lines) {
            CandleObject candle = getCandleObject(line);
            result.add(candle);
        }
        return result;
    }

    private static CandleObject getCandleObject(String line) {
        String[] tokens = line.split(",");

        Date openTime = null;
        Date closeTime = null;
        try {
            openTime = sdf.parse(tokens[0]);
            closeTime = sdf.parse(tokens[6]);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        double open = Double.parseDouble(tokens[1]);
        double high = Double.parseDouble(tokens[2]);
        double low = Double.parseDouble(tokens[3]);
        double close = Double.parseDouble(tokens[4]);
        double volume = Double.parseDouble(tokens[5]);

        return new CandleObject(openTime, open, high, low, close, volume, closeTime);
    }

    public static LinkedList<CandleObject> getHourCandles() {
        return readCandles(getHourLines());
    }

    public static LinkedList<CandleObject> getDayCandles() {
        return readCandles(getDayLines());
    }

    private static List<String> getHourLines() {
        try {
            return Files.readAllLines(Path.of(String.valueOf(sourceHour)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> getDayLines() {
        try {
            return Files.readAllLines(Path.of(String.valueOf(sourceDay)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
