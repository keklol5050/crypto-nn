package com.crypto.analysis.main;

import com.crypto.analysis.main.core.data_utils.select.StaticData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MetricsTest {
    public static void main(String[] args) throws IOException, ParseException {
        List<String> lines = Files.readAllLines(Path.of("C:\\static\\data\\btc\\metrics-BTCUSDT.csv"));
        lines.remove(0);
        List<String[]> linesArr = new ArrayList<>();
        for (String str : lines) {
            linesArr.add(str.split(","));
        }
        long fifteenMinutesInMillis = 15 * 60 * 1000;
        for (int i = 1; i < linesArr.size(); i++) {
            long current = StaticData.sdfFullISO.parse(linesArr.get(i)[0]).getTime();
            long prev = StaticData.sdfFullISO.parse(linesArr.get(i-1)[0]).getTime();
            if ((current-prev) != fifteenMinutesInMillis)
                System.out.println(Arrays.toString(linesArr.get(i)));
        }
    }
}
