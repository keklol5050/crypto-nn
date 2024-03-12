package com.crypto.analysis.main;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class FileDownloader {
    public static void main(String[] args) {
       for (int i = 1; i <= 31; i++) {
           String count = i>9 ? i+"" : "0"+i;
           String name = "BTCUSDT-metrics-2024-03-"+count+".zip";
           String fileUrl = "https://data.binance.vision/data/futures/um/daily/metrics/BTCUSDT/" + name;
           String destinationPath = "C:\\Users\\keklo\\OneDrive\\Рабочий стол\\Новая папка\\metrics\\" + name;
           try {
               downloadFile(fileUrl, destinationPath);
               System.out.println("Файл успешно загружен");
           } catch (IOException e) {
               e.printStackTrace();
               System.err.println("Ошибка при загрузке файла");
           }
       }
    }

    public static void downloadFile(String fileUrl, String destinationPath) throws IOException {
        URL url = new URL(fileUrl);
        try (InputStream in = url.openStream();
             FileOutputStream fos = new FileOutputStream(destinationPath)) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
    }
}