package com.crypto.analysis.main;

import com.crypto.analysis.main.data_utils.ndata.CSVHourAndDayTF;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TestData {
    public static void main(String[] args) throws IOException {
        Path sourceHour =  new File(CSVHourAndDayTF.class.getClassLoader().getResource("static/bitcoin_1h.txt").getFile()).toPath();
        Path sourceDay = new File(CSVHourAndDayTF.class.getClassLoader().getResource("static/bitcoin_1d.txt").getFile()).toPath();

        HashSet<String> dates = new HashSet<String>();
        List<String> lines = Files.readAllLines(sourceHour);
        for (String line : lines) {
            String date = line.split(",")[0].split(" ")[0]+" "+line.split(",")[0].split(" ")[1].split(":")[0];
            if (dates.contains(date)) {
                System.out.println(line);
            }
            dates.add(date);
        }

        dates = new HashSet<String>();
        lines = Files.readAllLines(sourceDay);
        for (String line : lines) {
            String date = line.split(",")[0].split(" ")[0]+" "+line.split(",")[0].split(" ")[1].split(":")[0];
            if (dates.contains(date)) {
                System.out.println(line);
            }
            dates.add(date);
        }
    }
}
