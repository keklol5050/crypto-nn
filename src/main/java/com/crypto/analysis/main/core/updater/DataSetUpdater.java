package com.crypto.analysis.main.core.updater;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.vo.CandleObject;
import com.crypto.analysis.main.core.vo.indication.FundingHistoryObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;

import static com.crypto.analysis.main.core.data_utils.select.StaticData.*;

public class DataSetUpdater {
    public void update(Coin coin, TimeFrame tf) throws IOException, ParseException {
        Path pathToCandles = switch (coin) {
            case BTCUSDT -> switch (tf) {
                case FIFTEEN_MINUTES -> pathToBTCCandles15m;
                case ONE_HOUR -> pathToBTCCandles1h;
                case FOUR_HOUR -> pathToBTCCandles4h;
            };
            default -> throw new IllegalStateException("Unexpected value: " + coin);
        };

        Path pathToFunding = switch (coin) {
            case BTCUSDT -> pathToBTCFunding;
            default -> throw new IllegalStateException("Unexpected value: " + coin);
        };

        Path pathToMetrics = switch (coin) {
            case BTCUSDT -> pathToBTCMetrics;
            default -> throw new IllegalStateException("Unexpected value: " + coin);
        };

        Path pathToFundValues = switch (coin) {
            case BTCUSDT -> switch (tf) {
                case FIFTEEN_MINUTES -> pathToBTCFund15m;
                case ONE_HOUR -> pathToBTCFund1h;
                case FOUR_HOUR -> pathToBTCFund4h;
            };
            default -> throw new IllegalStateException("Unexpected value: " + coin);
        };

        Path pathToDataSet = switch (coin) {
            case BTCUSDT -> switch (tf) {
                case FIFTEEN_MINUTES -> pathToFifteenMinutesBTCDataSet;
                case ONE_HOUR -> pathToOneHourBTCDataSet;
                case FOUR_HOUR -> pathToFourHourBTCDataSet;
            };
            default -> throw new IllegalStateException("Unexpected value: " + coin);
        };

        List<String> candleLines = Files.readAllLines(pathToCandles);
        List<String> fundList = Files.readAllLines(pathToFunding);
        List<String> metrics = Files.readAllLines(pathToMetrics);
        List<String> fundCr = Files.readAllLines(pathToFundValues);

        List<String> dom = Files.readAllLines(pathToBTCDOM);
        List<String> spx = Files.readAllLines(pathToSPX);
        List<String> dxy = Files.readAllLines(pathToDXY);
        List<String> dji = Files.readAllLines(pathToDJI);
        List<String> vix = Files.readAllLines(pathToVIX);
        List<String> ndx = Files.readAllLines(pathToNDX);
        List<String> xau = Files.readAllLines(pathToGOLD);

        candleLines.removeFirst();
        fundList.removeFirst();
        metrics.removeFirst();
        dom.removeFirst();
        spx.removeFirst();
        dxy.removeFirst();
        dji.removeFirst();
        vix.removeFirst();
        ndx.removeFirst();
        xau.removeFirst();
        fundCr.removeFirst();

        ArrayList<CandleObject> candles = new ArrayList<CandleObject>();
        for (String str : candleLines) {
            String[] tokens = str.split(",");
            Date openTime = new Date(Long.parseLong(tokens[0]));
            float open = Float.parseFloat(tokens[1]);
            float high = Float.parseFloat(tokens[2]);
            float low = Float.parseFloat(tokens[3]);
            float close = Float.parseFloat(tokens[4]);
            float volume = Float.parseFloat(tokens[5]);
            Date closeTime = new Date(Long.parseLong(tokens[6]));
            candles.add(new CandleObject(openTime, open, high, low, close, volume, closeTime));
        }

        TreeMap<Date, Float> fundingMap = new TreeMap<>();
        for (String str : fundList) {
            String[] tokens = str.split(",");
            fundingMap.put(new Date(Long.parseLong(tokens[0])), Float.parseFloat(tokens[2]));
        }
        FundingHistoryObject funding = new FundingHistoryObject(fundingMap);

        TreeMap<Date, Float> longShortMap = new TreeMap<Date, Float>();
        TreeMap<Date, Float> oiMap = new TreeMap<Date, Float>();

        for (String str : metrics) {
            String[] tokens = (str.split(","));
            Date date = sdfFullISO.parse(tokens[0]);
            float longShort = Float.parseFloat(tokens[6]);
            float oi = Float.parseFloat(tokens[2]);
            if (longShort == 0 || oi == 0) throw new RuntimeException();
            longShortMap.put(date, longShort);
            oiMap.put(date, oi);
        }

        TreeMap<Date, Float> domMap = new TreeMap<Date, Float>();
        for (String str : dom) {
            String[] tokens = str.split(",");
            Date openTime = new Date(Long.parseLong(tokens[0]));
            float open = Float.parseFloat(tokens[1]);
            domMap.put(openTime, open);
        }

        TreeMap<Date, Float> spxMap = new TreeMap<Date, Float>();
        for (String str : spx) {
            String[] tokens = str.split(";");
            Date openTime = sdfFullISO.parse(tokens[0]);
            float open = Float.parseFloat(tokens[1]);
            spxMap.put(openTime, open);
        }

        TreeMap<Date, Float> dxyMap = new TreeMap<Date, Float>();
        for (String str : dxy) {
            String[] tokens = str.split(";");
            Date openTime = sdfFullISO.parse(tokens[0]);
            float open = Float.parseFloat(tokens[1]);
            dxyMap.put(openTime, open);
        }

        TreeMap<Date, Float> djiMap = new TreeMap<Date, Float>();
        for (String str : dji) {
            String[] tokens = str.split(";");
            Date openTime = sdfFullISO.parse(tokens[0]);
            float open = Float.parseFloat(tokens[1]);
            djiMap.put(openTime, open);
        }


        TreeMap<Date, Float> vixMap = new TreeMap<Date, Float>();
        for (String str : vix) {
            String[] tokens = str.split(";");
            Date openTime = sdfFullISO.parse(tokens[0]);
            float open = Float.parseFloat(tokens[1]);
            vixMap.put(openTime, open);
        }

        TreeMap<Date, Float> ndxMap = new TreeMap<Date, Float>();
        for (String str : ndx) {
            String[] tokens = str.split(";");
            Date openTime = sdfFullISO.parse(tokens[0]);
            float open = Float.parseFloat(tokens[1]);
            ndxMap.put(openTime, open);
        }

        TreeMap<Date, Float> xauMap = new TreeMap<Date, Float>();
        for (String str : xau) {
            String[] tokens = str.split(";");
            Date openTime = sdfFullISO.parse(tokens[0]);
            float open = Float.parseFloat(tokens[1]);
            xauMap.put(openTime, open);
        }

        TreeMap<Date, float[]> fundCrMap = new TreeMap<Date, float[]>();
        for (String str : fundCr) {
            String[] tokens = str.split(",");
            Date openTime = sdfFullISO.parse(tokens[0]);
            fundCrMap.put(openTime, new float[]{
                    Float.parseFloat(tokens[1]),
                    Float.parseFloat(tokens[2]),
                    Float.parseFloat(tokens[3]),
                    Float.parseFloat(tokens[4]),
                    Float.parseFloat(tokens[5]),
                    Float.parseFloat(tokens[6]),
                    Float.parseFloat(tokens[7]),
                    Float.parseFloat(tokens[8])
            });
        }

        try (PrintWriter writer = new PrintWriter(String.valueOf(pathToDataSet))) {
            writer.println("open_time,open,high,low,close,volume,close_time,funding,open_interest,long_short_ratio,btc_dom,spx,dxy,dji,vix,ndx,gold,transactions_count,fee_value,fee_average,input_count,input_value,mined_value,output_count,output_value");

            for (CandleObject candle : candles) {
                Date current = candle.getOpenTime();
                float fund = funding.getValueForNearestDate(current);
                float oi = oiMap.get(current);
                float ls = longShortMap.get(current);
                float btcDOM = domMap.get(current);
                float spxV = spxMap.floorEntry(current).getValue();
                float dxyV = dxyMap.floorEntry(current).getValue();
                float djiV = djiMap.floorEntry(current).getValue();
                float vixV = vixMap.floorEntry(current).getValue();
                float ndxV = ndxMap.floorEntry(current).getValue();
                float xauV = xauMap.floorEntry(current).getValue();
                float[] trans = fundCrMap.floorEntry(current).getValue();
                String result = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", sdfFullISO.format(current), candle.getOpen(), candle.getHigh(),
                        candle.getLow(), candle.getClose(), candle.getVolume(), sdfFullISO.format(candle.getCloseTime()), fund, oi, ls, btcDOM,
                        spxV, dxyV, djiV, vixV, ndxV, xauV, trans[0], trans[1], trans[2], trans[3], trans[4], trans[5], trans[6], trans[7]);
                writer.println(result);
                writer.flush();
            }
        }

        System.out.println("Updated " + coin + " data set at path " + pathToDataSet);
    }
}
