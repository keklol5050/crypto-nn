package com.crypto.analysis.main;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;

public class FileDownloader {
    public static void main(String[] args) {
        String baseUrl = "https://data.binance.vision/data/futures/um/daily/klines/BTCUSDT/4h/BTCUSDT-4h-2024-03-%s.zip";
        String saveDirectory = "C:\\Users\\keklo\\OneDrive\\Рабочий стол\\Новая папка\\ll\\ll\\";

        DecimalFormat decimalFormat = new DecimalFormat("00");

        for (int i = 1; i <= 31; i++) {
            String day = decimalFormat.format(i);
            String url = String.format(baseUrl, day);
            String fileName = "BTCUSDT-4h-2024-03-" + day + ".zip";
            String filePath = saveDirectory + fileName;

            try {
                downloadFile(url, filePath);
                System.out.println("File downloaded successfully: " + fileName);
            } catch (Exception e) {
                System.err.println("Failed to download file: " + fileName);
                e.printStackTrace();
            }
        }
    }

    private static void downloadFile(String url, String saveFilePath) throws Exception {
        URL downloadUrl = new URL(url);
        try (InputStream inputStream = downloadUrl.openStream();
             FileOutputStream outputStream = new FileOutputStream(saveFilePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }
}