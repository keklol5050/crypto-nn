package com.crypto.analysis.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

public class Invertor {
    public static void main(String[] args) throws IOException {
        String url = "C:\\static\\data\\btc\\funding-BTCUSDT.csv";
        List<String> lines = Files.readAllLines(Path.of(url));

        try (PrintWriter writer = new PrintWriter(url)) {
            writer.println(lines.remove(0));
            for (String str : lines) {
                String[] tokens = str.split(",");
                StringBuilder builder = new StringBuilder(tokens[0]);
                builder.setCharAt(builder.length() - 1, '0');
                builder.setCharAt(builder.length() - 2, '0');
                Date date = new Date(Long.parseLong(builder.toString()));
                writer.println(String.format("%s,%s,%s", date.getTime(), tokens[1], tokens[2]));
            }
        }

    }
}
