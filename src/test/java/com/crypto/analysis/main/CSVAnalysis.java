package com.crypto.analysis.main;

import org.apache.commons.csv.*;

import java.io.FileReader;
import java.io.Reader;
import java.util.*;

public class CSVAnalysis {
    public static void main(String[] args) throws Exception {
        Reader in = new FileReader("C:\\static\\data\\btc\\metrics-BTCUSDT.csv");
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
        LinkedHashMap<String, List<Double>> columnValues = new LinkedHashMap<>();
        Set<Integer> skipColumns = new HashSet<>(Arrays.asList(0, 1,3,4,5)); // Колонки для пропуска

        for (CSVRecord record : records) {
            Map<String, String> recordMap = record.toMap();
            int columnIndex = 0;
            for (String column : recordMap.keySet()) {
                if (skipColumns.contains(columnIndex)) {
                    columnIndex++;
                    continue;
                }
                String value = record.get(column);
                if (!columnValues.containsKey(column)) {
                    columnValues.put(column, new ArrayList<>());
                }
                try {
                    columnValues.get(column).add(Double.parseDouble(value));
                } catch (NumberFormatException e) {
                    System.out.println("Skipping non-numeric value: " + value);
                }
                columnIndex++;
            }
        }

        for (String column : columnValues.keySet()) {
            List<Double> values = columnValues.get(column);
            double min = Collections.min(values);
            double max = Collections.max(values);
            double average = values.stream().mapToDouble(val -> val).average().orElse(0.0);

            System.out.println("Column: " + column);
            System.out.println("Min: " + min);
            System.out.println("Max: " + max);
            System.out.println("Average: " + average);
            System.out.println("-------------------");
        }
    }
}