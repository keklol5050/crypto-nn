package com.crypto.analysis.main.core.data_utils.normalizers.robust;

import com.crypto.analysis.main.core.data_utils.select.StaticData;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.utils.SentimentUtil;
import com.crypto.analysis.main.core.vo.indication.SentimentHistoryObject;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.time.ZoneId;
import java.util.*;

public class NormalizerHelper {
    static ArrayList<Double> fundingList;
    static ArrayList<Double> longShortList;
    static ArrayList<Double> openInterestList;
    static ArrayList<Double> buySellRatioList;
    static ArrayList<Double> domList;
    static ArrayList<Double> spxList;
    static ArrayList<Double> dxyList;
    static ArrayList<Double> djiList;
    static ArrayList<Double> vixList;
    static ArrayList<Double> ndxList;
    static ArrayList<Double> xauList;
    static ArrayList<Double> sentimentMean;
    static ArrayList<Double> sentimentSum;

    private static boolean isInitialized;

    @SneakyThrows
    public static void loadData(Coin coin) {
        if (isInitialized) return;

        fundingList = new ArrayList<>();
        longShortList = new ArrayList<>();
        openInterestList = new ArrayList<>();
        buySellRatioList = new ArrayList<>();
        domList = new ArrayList<>();
        spxList = new ArrayList<>();
        dxyList = new ArrayList<>();
        djiList = new ArrayList<>();
        vixList = new ArrayList<>();
        ndxList = new ArrayList<>();
        xauList = new ArrayList<>();
        sentimentMean = new ArrayList<>();
        sentimentSum = new ArrayList<>();

        List<String> fundList = switch (coin) {
            case BTCUSDT ->
                    Files.readAllLines(StaticData.pathToBTCFunding);
            default -> throw new IllegalArgumentException();
        };
        List<String> metrics = switch (coin) {
            case BTCUSDT ->
                    Files.readAllLines(StaticData.pathToBTCMetrics);
            default -> throw new IllegalArgumentException();
        };

        List<String> dom = Files.readAllLines(StaticData.pathToBTCDOM);
        List<String> spx = Files.readAllLines(StaticData.pathToSPX);
        List<String> dxy = Files.readAllLines(StaticData.pathToDXY);
        List<String> dji = Files.readAllLines(StaticData.pathToDJI);
        List<String> vix = Files.readAllLines(StaticData.pathToVIX);
        List<String> ndx = Files.readAllLines(StaticData.pathToNDX);
        List<String> xau = Files.readAllLines(StaticData.pathToGOLD);

        fundList.remove(0);
        metrics.remove(0);
        dom.remove(0);
        spx.remove(0);
        dxy.remove(0);
        dji.remove(0);
        vix.remove(0);
        ndx.remove(0);
        xau.remove(0);


        for (String str : fundList) {
            String[] tokens = str.split(",");
            double funding = Double.parseDouble(tokens[2]);
            fundingList.add(funding);
        }

        for (String str : metrics) {
            String[] tokens = (str.split(","));
            double longShort = Double.parseDouble(tokens[6]);
            longShortList.add(longShort);
            double oi = Double.parseDouble(tokens[2]);
            openInterestList.add(oi);
            double bsr = Double.parseDouble(tokens[7]);
            buySellRatioList.add(bsr);
        }

        for (String str : dom) {
            String[] tokens = str.split(",");
            double open = Double.parseDouble(tokens[1]);
            domList.add(open);
        }
        for (String str : spx) {
            String[] tokens = str.split(";");
            double open = Double.parseDouble(tokens[1]);
            spxList.add(open);
        }

        for (String str : dxy) {
            String[] tokens = str.split(";");
            double open = Double.parseDouble(tokens[1]);
            dxyList.add(open);
        }

        for (String str : dji) {
            String[] tokens = str.split(";");
            double open = Double.parseDouble(tokens[1]);
            djiList.add(open);
        }

        for (String str : vix) {
            String[] tokens = str.split(";");
            double open = Double.parseDouble(tokens[1]);
            vixList.add(open);
        }

        for (String str : ndx) {
            String[] tokens = str.split(";");
            double open = Double.parseDouble(tokens[1]);
            ndxList.add(open);
        }

        for (String str : xau) {
            String[] tokens = str.split(";");
            double open = Double.parseDouble(tokens[1]);
            xauList.add(open);
        }

        SentimentHistoryObject sentiment = SentimentUtil.getData();
        for (Map.Entry<Date, double[]> entry : sentiment.getMap().entrySet()) {
            if (entry.getKey().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isBefore(StaticData.START_DATE)) continue;
            sentimentMean.add(entry.getValue()[0]);
            sentimentSum.add(entry.getValue()[1]);
        }
        isInitialized = true;
    }

    public static HashMap<Integer, ArrayList<Double>> getMap() {
        HashMap<Integer, ArrayList<Double>> map = new HashMap<>();
        map.put(54, fundingList);
        map.put(55, openInterestList);
        map.put(56, longShortList);
        map.put(57, buySellRatioList);
        map.put(58, spxList);
        map.put(59, dxyList);
        map.put(60, djiList);
        map.put(61, vixList);
        map.put(62, ndxList);
        map.put(63, xauList);
        map.put(64, domList);
        map.put(65, sentimentMean);
        map.put(66, sentimentSum);
        return map;
    }

    public static void restart() {
        isInitialized = false;
    }

    public static void main(String[] args) {
        NormalizerHelper.loadData(Coin.BTCUSDT);
        HashMap<Integer, ArrayList<Double>> volatileData = NormalizerHelper.getMap();

    }

}

