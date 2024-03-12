package com.crypto.analysis.main.ndata;

import com.crypto.analysis.main.data_utils.IndicatorsDataUtil;
import com.crypto.analysis.main.enumerations.Coin;
import com.crypto.analysis.main.enumerations.TimeFrame;
import com.crypto.analysis.main.vo.CandleObject;
import com.crypto.analysis.main.vo.DataObject;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Getter
public class CSVDataSet {
    private static final Path pathToFifteenDataSet = new File(CSVDataSet.class.getClassLoader().getResource("static/bitcoin_15m.csv").getFile()).toPath();

    private final Path path;
    private final Coin coin;
    private final TimeFrame interval;
    private final LinkedList<DataObject> data;

    private boolean isInitialized = false;
    public CSVDataSet(Coin coin, TimeFrame interval) {
        this.coin = coin;
        this.interval = interval;
        this.path = switch (interval) {
            case FIFTEEN_MINUTES -> pathToFifteenDataSet;
            default -> throw new RuntimeException("Invalid time frame");
        };
        data = new LinkedList<>();
    }

    public void load() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC+0"));

        LinkedList<DataObject> localData = new LinkedList<DataObject>();
        LinkedList<CandleObject> candles = new LinkedList<CandleObject>();
        try {
            List<String> lines = Files.readAllLines(path);
            lines.remove(0);
            for (String line : lines) {
                String[] tokens = line.split(",");

                Date openTime = sdf.parse(tokens[0]);
                double open = Double.parseDouble(tokens[1]);
                double high = Double.parseDouble(tokens[2]);
                double low = Double.parseDouble(tokens[3]);
                double close = Double.parseDouble(tokens[4]);
                double volume = Double.parseDouble(tokens[5]);
                Date closeTime = sdf.parse(tokens[6]);
                CandleObject candle = new CandleObject(openTime, open, high, low, close, volume, closeTime);

                double fundingRate = Double.parseDouble(tokens[7]);
                double openInterest = Double.parseDouble(tokens[8]);
                double longShortRatio = Double.parseDouble(tokens[9]);
                double sellBuyRatio = Double.parseDouble(tokens[10]);

                DataObject object = new DataObject(coin, interval);
                object.setCandle(candle);
                object.setCurrentFundingRate(fundingRate);
                object.setCurrentOpenInterest(openInterest);
                object.setLongShortRatio(longShortRatio);
                object.setBuySellRatio(sellBuyRatio);
                localData.add(object);
                candles.add(candle);
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }

        IndicatorsDataUtil util = new IndicatorsDataUtil(Collections.unmodifiableList(candles));
        for (int i = 0; i < localData.size(); i++) {
            DataObject object = localData.get(i);
            object.setCurrentIndicators(util.getIndicators(i));
            this.data.add(object);
        }
        isInitialized = true;
    }

    public static void main(String[] args) {
        CSVDataSet dataSet = new CSVDataSet(Coin.BTCUSDT, TimeFrame.FIFTEEN_MINUTES);
        dataSet.load();
        System.out.println(dataSet.data);
    }
}
