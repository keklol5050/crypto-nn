package com.crypto.analysis.main;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DataInterpolation {
    public static void main(String[] args) {
        try {
            // Чтение данных из файла
            ArrayList<String[]> data = readDataFromFile("input.csv");

            // Интерполяция данных
            ArrayList<String[]> interpolatedData = interpolateData(data);

            // Запись интерполированных данных в файл
            writeDataToFile(interpolatedData, "output.csv");

            System.out.println("Интерполяция завершена успешно.");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<String[]> readDataFromFile(String filename) throws IOException {
        ArrayList<String[]> data = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            data.add(parts);
        }
        reader.close();
        return data;
    }

    private static ArrayList<String[]> interpolateData(ArrayList<String[]> data) throws ParseException {
        ArrayList<String[]> interpolatedData = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date previousDate = null;
        double previousPrice = 0;

        for (String[] row : data) {
            Date currentDate = dateFormat.parse(row[0]);
            double currentPrice = Double.parseDouble(row[2]);

            if (previousDate != null) {
                long diffInMinutes = (currentDate.getTime() - previousDate.getTime()) / (1000 * 60);
                if (diffInMinutes > 5) { // Если пропущено более 5 минут
                    for (long i = 1; i < diffInMinutes; i++) {
                        double interpolatedPrice = previousPrice +
                                (currentPrice - previousPrice) * i / diffInMinutes;
                        Date interpolatedDate = new Date(previousDate.getTime() + i * 60 * 1000);
                        interpolatedData.add(new String[]{
                                dateFormat.format(interpolatedDate),
                                row[1],
                                String.valueOf(interpolatedPrice),
                                // Для остальных столбцов добавьте интерполируемые значения по необходимости
                        });
                    }
                }
            }

            interpolatedData.add(row);
            previousDate = currentDate;
            previousPrice = currentPrice;
        }

        return interpolatedData;
    }

    private static void writeDataToFile(ArrayList<String[]> data, String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (String[] row : data) {
            writer.write(String.join(",", row));
            writer.newLine();
        }
        writer.close();
    }
}