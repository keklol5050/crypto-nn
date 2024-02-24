package com.crypto.analysis.main.data_utils.nn;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.function.UnaryOperator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Solution {

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);

        int epochs = 0;
        do {
            System.out.print("Enter the number of indexings: ");
            epochs = sc.nextInt();
            if (epochs < 11) System.out.println("Must be more >11! Try again.");
        } while (epochs < 11);
        sc.close();
        System.out.println("Indexing..");
        digits(epochs);
    }


    private static void digits(int epochs) throws IOException {
        UnaryOperator<Double> sigmoid = x -> 1 / (1 + Math.exp(-x));
        UnaryOperator<Double> dsigmoid = y -> y * (1 - y);
        NeuralNetwork nn = new NeuralNetwork(0.001, sigmoid, dsigmoid, 784, 512, 128, 32, 10);

        int samples = 60000;
        BufferedImage[] images = new BufferedImage[samples];
        int[] digits = new int[samples];

        String folderPath = "Train/";
        File[] imagesFiles = new File[samples];

        String className = Solution.class.getName().replace('.', '/');
        String classJar = Solution.class.getResource("/" + className + ".class").toString();
        if (classJar.startsWith("jar:")) {
            try (JarFile jarFile = new JarFile(new File(Solution.class.getProtectionDomain().getCodeSource().getLocation().getPath()))) {
                int index = 0;
                for (JarEntry entry : jarFile.stream().filter(entry -> entry.getName().startsWith(folderPath)).toArray(JarEntry[]::new)) {
                    if (index == samples) break;
                    try {
                        String fileName = Paths.get(entry.getName()).getFileName().toString();
                        if (!fileName.contains("num")) continue;

                        digits[index] = Integer.parseInt(fileName.charAt(10) + "");

                        File tempFile = createTempFileFromJarEntry(jarFile, entry);
                        imagesFiles[index] = tempFile;

                        System.out.println(tempFile.getAbsoluteFile() + " " + fileName + " Num:" + digits[index] + " " + (index + 1));
                        index++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < samples; i++) {
                images[i] = ImageIO.read(imagesFiles[i]);
                System.out.println(images[i] + " " + i);
            }
        } else {
            imagesFiles = new File("Resources/Train").listFiles();
            for (int i = 0; i < samples; i++) {
                images[i] = ImageIO.read(imagesFiles[i]);
                digits[i] = Integer.parseInt(imagesFiles[i].getName().charAt(10) + "");
            }
        }
        double[][] inputs = new double[samples][784];
        for (int i = 0; i < samples; i++) {
            if (images[i] != null) {
                for (int x = 0; x < 28; x++) {
                    for (int y = 0; y < 28; y++) {
                        inputs[i][x + y * 28] = (images[i].getRGB(x, y) & 0xff) / 255.0;
                    }
                }
            } else {
                System.out.println("images" + i + " = null" + "\nrefactoring");
                images[i] = images[(int) (Math.random() * samples)];
            }
        }

        double error = 0;
        for (int i = 1; i < epochs; i++) {
            int right = 0;
            double errorSum = 0;
            int batchSize = 100;
            for (int j = 0; j < batchSize; j++) {
                int imgIndex = (int) (Math.random() * samples);
                double[] targets = new double[10];
                int digit = digits[imgIndex];
                targets[digit] = 1;

                double[] outputs = nn.feedForward(inputs[imgIndex]);
                int maxDigit = 0;
                double maxDigitWeight = -1;
                for (int k = 0; k < 10; k++) {
                    if (outputs[k] > maxDigitWeight) {
                        maxDigitWeight = outputs[k];
                        maxDigit = k;
                    }
                }
                if (digit == maxDigit) right++;
                for (int k = 0; k < 10; k++) {
                    errorSum += (targets[k] - outputs[k]) * (targets[k] - outputs[k]);
                }
                nn.backpropagation(targets);
            }
            System.out.println("Index: " + i + ". True: " + right + ". Error: " + errorSum);
            if (i >= epochs - 10 && i < epochs - 1) {
                error = (error + errorSum) / 2;
            }
        }
        System.out.println("Total indexed: " + epochs + "\nFinal accuracy:" + (100 - Math.ceil(error)) + "%");

        FormDigits f = new FormDigits(nn);
        System.out.println("Removing cache");
        new Thread(f).start();
        for (File file : imagesFiles) {
            file.delete();
        }
        System.out.println("Cache deleted succesfully");
    }

    private static File createTempFileFromJarEntry(JarFile jarFile, JarEntry entry) throws IOException {
        String entryName = entry.getName();
        File tempFile = File.createTempFile("temp", ".png");
        tempFile.deleteOnExit();

        try (InputStream is = jarFile.getInputStream(entry);
             OutputStream os = new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }

        return tempFile;
    }
}