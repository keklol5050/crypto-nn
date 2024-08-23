package com.crypto.analysis.main.core.updater;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.IndicatorsDataUtil;
import com.crypto.analysis.main.core.data_utils.utils.PropertiesUtil;
import com.crypto.analysis.main.core.data_utils.utils.SentimentUtil;
import com.crypto.analysis.main.core.vo.CandleObject;
import com.crypto.analysis.main.core.vo.DataObject;
import com.crypto.analysis.main.core.vo.FundamentalCryptoDataObject;
import com.crypto.analysis.main.core.vo.FundamentalStockObject;
import com.crypto.analysis.main.core.vo.indication.FundingHistoryObject;
import com.crypto.analysis.main.core.vo.indication.SentimentHistoryObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;

import static com.crypto.analysis.main.core.data_utils.select.StaticUtils.sdfFullISO;
import static com.crypto.analysis.main.core.updater.FundamentalUpdater.*;

public class DataSetUpdater {
    private static final Logger logger = LoggerFactory.getLogger(CandlesUpdater.class);
    private static DataSetUpdater instance;

    private TreeMap<Date, Float> domMap;
    private TreeMap<Date, Float> ethBtcMap;
    private TreeMap<Date, float[]> spxMap;
    private TreeMap<Date, float[]> dxyMap;
    private TreeMap<Date, float[]> djiMap;
    private TreeMap<Date, float[]> vixMap;
    private TreeMap<Date, float[]> ndxMap;
    private TreeMap<Date, float[]> xauMap;
    private SentimentHistoryObject sentiment;

    private DataSetUpdater() {
    }

    public static DataSetUpdater getInstance() {
        if (instance == null) {
            instance = new DataSetUpdater();
            try {
                instance.init();
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    private void init() throws IOException, ParseException {
        List<String> dom = Files.readAllLines(pathToBTCDOM);
        List<String> spx = Files.readAllLines(pathToSPX);
        List<String> dxy = Files.readAllLines(pathToDXY);
        List<String> dji = Files.readAllLines(pathToDJI);
        List<String> vix = Files.readAllLines(pathToVIX);
        List<String> ndx = Files.readAllLines(pathToNDX);
        List<String> xau = Files.readAllLines(pathToGOLD);

        Path pathToBTCCandles = Path.of(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getParent() +
                PropertiesUtil.getProperty("data.data_path") + Coin.BTCUSDT + "/candles/" + TimeFrame.FIFTEEN_MINUTES.getTimeFrame() + ".csv");
        Path pathToETHCandles = Path.of(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getParent() +
                PropertiesUtil.getProperty("data.data_path") + Coin.ETHUSDT + "/candles/" + TimeFrame.FIFTEEN_MINUTES.getTimeFrame() + ".csv");

        List<String> btcCandleLines = Files.readAllLines(pathToBTCCandles);
        List<String> ethCandleLines = Files.readAllLines(pathToETHCandles);

        dom.removeFirst();
        spx.removeFirst();
        dxy.removeFirst();
        dji.removeFirst();
        vix.removeFirst();
        ndx.removeFirst();
        xau.removeFirst();
        btcCandleLines.removeFirst();
        ethCandleLines.removeFirst();

        domMap = new TreeMap<Date, Float>();
        for (String str : dom) {
            String[] tokens = str.split(",");
            Date openTime = sdfFullISO.parse(tokens[0]);
            float close = Float.parseFloat(tokens[4]);
            domMap.put(openTime, close);
        }

        spxMap = new TreeMap<>();
        for (String str : spx) {
            String[] tokens = str.split(",");
            Date openTime = sdfFullISO.parse(tokens[0]);
            float open = Float.parseFloat(tokens[1]);
            float high = Float.parseFloat(tokens[2]);
            float low = Float.parseFloat(tokens[3]);
            float close = Float.parseFloat(tokens[4]);
            float volume = Float.parseFloat(tokens[5]);
            spxMap.put(openTime, new float[]{open, high, low, close, volume});
        }

        dxyMap = new TreeMap<>();
        for (String str : dxy) {
            String[] tokens = str.split(",");
            Date openTime = sdfFullISO.parse(tokens[0]);
            float open = Float.parseFloat(tokens[1]);
            float high = Float.parseFloat(tokens[2]);
            float low = Float.parseFloat(tokens[3]);
            float close = Float.parseFloat(tokens[4]);
            float volume = Float.parseFloat(tokens[5]);
            dxyMap.put(openTime, new float[]{open, high, low, close, volume});
        }

        djiMap = new TreeMap<>();
        for (String str : dji) {
            String[] tokens = str.split(",");
            Date openTime = sdfFullISO.parse(tokens[0]);
            float open = Float.parseFloat(tokens[1]);
            float high = Float.parseFloat(tokens[2]);
            float low = Float.parseFloat(tokens[3]);
            float close = Float.parseFloat(tokens[4]);
            float volume = Float.parseFloat(tokens[5]);
            djiMap.put(openTime, new float[]{open, high, low, close, volume});
        }


        vixMap = new TreeMap<>();
        for (String str : vix) {
            String[] tokens = str.split(",");
            Date openTime = sdfFullISO.parse(tokens[0]);
            float open = Float.parseFloat(tokens[1]);
            float high = Float.parseFloat(tokens[2]);
            float low = Float.parseFloat(tokens[3]);
            float close = Float.parseFloat(tokens[4]);
            float volume = Float.parseFloat(tokens[5]);
            vixMap.put(openTime, new float[]{open, high, low, close, volume});
        }

        ndxMap = new TreeMap<>();
        for (String str : ndx) {
            String[] tokens = str.split(",");
            Date openTime = sdfFullISO.parse(tokens[0]);
            float open = Float.parseFloat(tokens[1]);
            float high = Float.parseFloat(tokens[2]);
            float low = Float.parseFloat(tokens[3]);
            float close = Float.parseFloat(tokens[4]);
            float volume = Float.parseFloat(tokens[5]);
            ndxMap.put(openTime, new float[]{open, high, low, close, volume});
        }

        xauMap = new TreeMap<>();
        for (String str : xau) {
            String[] tokens = str.split(",");
            Date openTime = sdfFullISO.parse(tokens[0]);
            float open = Float.parseFloat(tokens[1]);
            float high = Float.parseFloat(tokens[2]);
            float low = Float.parseFloat(tokens[3]);
            float close = Float.parseFloat(tokens[4]);
            float volume = Float.parseFloat(tokens[5]);
            xauMap.put(openTime, new float[]{open, high, low, close, volume});
        }

        TreeMap<Date, Float> btcValsTemp = new TreeMap<>();
        for (String str : btcCandleLines) {
            String[] tokens = str.split(",");
            Date openTime = sdfFullISO.parse(tokens[0]);
            float close = Float.parseFloat(tokens[4]);
            btcValsTemp.put(openTime, close);
        }
        TreeMap<Date, Float> ethValsTemp = new TreeMap<>();
        for (String str : ethCandleLines) {
            String[] tokens = str.split(",");
            Date openTime = sdfFullISO.parse(tokens[0]);
            float close = Float.parseFloat(tokens[4]);
            ethValsTemp.put(openTime, close);
        }

        ethBtcMap = new TreeMap<Date, Float>();
        for (Map.Entry<Date, Float> entry : btcValsTemp.entrySet()) {
            Date curr = entry.getKey();
            if (!ethValsTemp.containsKey(curr))
                throw new IllegalStateException("Invalid data list for ETH/BTC rate");

            Float btcEntry = entry.getValue();
            Float ethEntry = ethValsTemp.get(curr);

            Float ethBtcValue = ethEntry / btcEntry;
            ethBtcMap.put(entry.getKey(), ethBtcValue);
        }

        sentiment = SentimentUtil.getData();
    }

    public void update(Coin coin, TimeFrame tf) throws IOException, ParseException {
        logger.info("Starting updating data set for coin {}, time frame {}", coin, tf);

        Path pathToCandles = Path.of(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getParent() + PropertiesUtil.getProperty("data.data_path") + coin + "/candles/" + tf.getTimeFrame() + ".csv");
        Path pathToFunding = Path.of(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getParent() + PropertiesUtil.getProperty("data.data_path") + coin + "/funding.csv");
        Path pathToMetrics = Path.of(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getParent() + PropertiesUtil.getProperty("data.data_path") + coin + "/metrics.csv");
        Path pathToFundValues = Path.of(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getParent() + PropertiesUtil.getProperty("data.data_path") + coin + "/fund/" + tf.getTimeFrame() + ".csv");
        Path pathToDataSet = Path.of(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getParent() + PropertiesUtil.getProperty("data.datasets_path") + coin + "/" + tf.getTimeFrame() + ".csv");

        List<String> candleLines = Files.readAllLines(pathToCandles);
        List<String> fundList = Files.readAllLines(pathToFunding);
        List<String> metrics = Files.readAllLines(pathToMetrics);
        List<String> fundCr = Files.readAllLines(pathToFundValues);

        candleLines.removeFirst();
        fundList.removeFirst();
        metrics.removeFirst();
        fundCr.removeFirst();

        ArrayList<CandleObject> candles = new ArrayList<CandleObject>();
        for (String str : candleLines) {
            String[] tokens = str.split(",");
            Date openTime = sdfFullISO.parse(tokens[0]);
            float open = Float.parseFloat(tokens[1]);
            float high = Float.parseFloat(tokens[2]);
            float low = Float.parseFloat(tokens[3]);
            float close = Float.parseFloat(tokens[4]);
            float volume = Float.parseFloat(tokens[5]);
            Date closeTime = sdfFullISO.parse(tokens[6]);
            candles.add(new CandleObject(openTime, open, high, low, close, volume, closeTime));
        }
        TreeMap<Date, Float> fundingMap = new TreeMap<>();
        for (String str : fundList) {
            String[] tokens = str.split(",");
            fundingMap.put(sdfFullISO.parse(tokens[0]), Float.parseFloat(tokens[1]));
        }
        FundingHistoryObject funding = new FundingHistoryObject(fundingMap);

        TreeMap<Date, Float> longShortMap = new TreeMap<Date, Float>();
        TreeMap<Date, Float> oiMap = new TreeMap<Date, Float>();
        for (String str : metrics) {
            String[] tokens = (str.split(","));
            Date date = sdfFullISO.parse(tokens[0]);
            float oi = Float.parseFloat(tokens[1]);
            float longShort = Float.parseFloat(tokens[2]);
            if (longShort == 0 || oi == 0)
                throw new RuntimeException();
            longShortMap.put(date, longShort);
            oiMap.put(date, oi);
        }

        int max = coin.getFundCols().length;
        TreeMap<Date, float[]> fundCrMap = new TreeMap<Date, float[]>();
        for (String str : fundCr) {
            String[] tokens = str.split(",");
            Date openTime = sdfFullISO.parse(tokens[0]);

            float[] values = new float[max];
            for (int i = 0; i < max; i++) {
                values[i] = Float.parseFloat(tokens[i + 1]);
            }
            fundCrMap.put(openTime, values);
        }

        ArrayList<DataObject> localData = new ArrayList<DataObject>();
        IndicatorsDataUtil util = new IndicatorsDataUtil(candles);

        for (int i = 0; i < candles.size(); i++) {
            CandleObject candle = candles.get(i);
            Date current = candle.getOpenTime();

            DataObject obj = new DataObject(coin, tf);
            obj.setCandle(candle);

            obj.setCurrentFundingRate(funding.getValueForNearestDate(current));
            obj.setCurrentOpenInterest(oiMap.get(current));
            obj.setLongShortRatio(longShortMap.get(current));
            obj.setETHBTCPrice(ethBtcMap.get(current));
            obj.setBTCDomination(domMap.get(current));

            FundamentalCryptoDataObject fCrypto = new FundamentalCryptoDataObject(coin, fundCrMap.floorEntry(current).getValue());
            obj.setCryptoFundamental(fCrypto);

            FundamentalStockObject stock = new FundamentalStockObject();
            stock.setSPX(spxMap.floorEntry(current).getValue());
            stock.setDXY(dxyMap.floorEntry(current).getValue());
            stock.setDJI(djiMap.floorEntry(current).getValue());
            stock.setVIX(vixMap.floorEntry(current).getValue());
            stock.setNDX(ndxMap.floorEntry(current).getValue());
            stock.setGOLD(xauMap.floorEntry(current).getValue());
            obj.setFundamentalData(stock);

            float[] sentValues = sentiment.getValueForNearestDate(candle.getOpenTime());
            obj.setSentimentMean(sentValues[0]);
            obj.setSentimentSum(sentValues[1]);

            obj.setCurrentIndicators(util.getIndicators(i));

            localData.add(obj);
        }
        writeData(pathToDataSet, localData);
        logger.info("Updated {} data set at path {}", coin, pathToDataSet);
    }

    public static void writeData(Path path, ArrayList<DataObject> data) {
        DataObject firstEntry = data.getFirst();

        String fundCols = String.join(",", firstEntry.getCoin().getFundCols()) + ',';

        try (PrintWriter writer = new PrintWriter(String.valueOf(path))) {
            writer.println("ds,open,high,low,close,volume," + fundCols +
                    "SMA200,SMA99,SMA60,SMA50,SMA30,SMA15,SMA10,WMA200,WMA99,WMA60,WMA50,WMA30,WMA15,WMA10,EMA200,EMA99,EMA60,EMA50,EMA30,EMA15,EMA10,MMA,PSAR,VWAP,SPANA,SPANB,KIJUN,TENKAN," +
                    "RSI,MACD12,MACD24,STOCHK,STOCHD,CCI,ADX,AROONUP,AROONDOWN,STOCHRSI,ATR,DPO,WILLR,MI,CMO,ROC,RAVI," +
                    "eth_btc,btc_dom,open_interest,long_short_ratio,funding_rate,sentiment_mean,sentiment_sum," +
                    "SPX,DXY,DJI,VIX,NDX,GOLD");
            for (DataObject obj : data) {
                writer.println(String.format("%s,", sdfFullISO.format(obj.getCandle().getOpenTime())) + Arrays.toString(obj.getParamArray()).replaceAll("[\\[\\] ]", ""));
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
