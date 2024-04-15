package com.crypto.analysis.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CompareCSV {
    public static void main(String[] args) throws IOException {
        BufferedReader br1 = new BufferedReader(new FileReader("C:\\static\\datasets\\btc\\bitcoin_1h2.csv"));
        BufferedReader br2 = new BufferedReader(new FileReader("C:\\static\\datasets\\btc\\bitcoin_1h.csv"));

        String line1 = br1.readLine();
        String line2 = br2.readLine();

        boolean areEqual = true;
        int lineNum = 1;

        while (line1 != null || line2 != null) {
            if(line1 == null || line2 == null) {
                areEqual = false;
                break;
            }
            else if(! line1.equalsIgnoreCase(line2)) {
                areEqual = false;
                System.out.println("Difference in line "+ lineNum +":");
                System.out.println("\tFile1: " + line1);
                System.out.println("\tFile2: " + line2);
            }

            line1 = br1.readLine();
            line2 = br2.readLine();
            lineNum++;
        }

        if(areEqual) {
            System.out.println("Two files have same content.");
        }
        else {
            System.out.println("Files are not equal.");
        }

        br1.close();
        br2.close();
    }
}