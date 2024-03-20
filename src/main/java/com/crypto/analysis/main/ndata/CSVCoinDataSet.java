package com.crypto.analysis.main.ndata;

import com.crypto.analysis.main.data_utils.utils.IndicatorsDataUtil;
import com.crypto.analysis.main.data_utils.utils.SentimentUtil;
import com.crypto.analysis.main.data_utils.select.coin.Coin;
import com.crypto.analysis.main.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.vo.FundamentalStockObject;
import com.crypto.analysis.main.fundamental.stock.TimeFrameConverter;
import com.crypto.analysis.main.vo.CandleObject;
import com.crypto.analysis.main.vo.DataObject;
import com.crypto.analysis.main.vo.indication.SentimentHistoryObject;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.crypto.analysis.main.data_utils.select.StaticData.SKIP_NUMBER;

@Getter
public class CSVCoinDataSet {
    private static final Path pathToFifteenDataSet = new File(Objects.requireNonNull(CSVCoinDataSet.class.getClassLoader().getResource("static/bitcoin_15m.csv")).getFile()).toPath();
    private static final Path pathToHourDataSet = new File(Objects.requireNonNull(CSVCoinDataSet.class.getClassLoader().getResource("static/bitcoin_1h.csv")).getFile()).toPath();

    private final Path path;
    private final Coin coin;
    private final TimeFrame interval;
    private final LinkedList<DataObject> data;

    private boolean isInitialized = false;
    public CSVCoinDataSet(Coin coin, TimeFrame interval) {
        this.coin = coin;
        this.interval = interval;
        this.path = switch (interval) {
            case FIFTEEN_MINUTES -> pathToFifteenDataSet;
            case ONE_HOUR -> pathToHourDataSet;
            default -> throw new RuntimeException("Invalid time frame");
        };
        data = new LinkedList<>();
    }

    public void load() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC+0"));

        LinkedList<DataObject> localData = new LinkedList<DataObject>();
        LinkedList<CandleObject> candles = new LinkedList<CandleObject>();

        SentimentHistoryObject sentiment = SentimentUtil.getData();

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

                double fundingRate = Double.parseDouble(tokens[7])*100;
                double openInterest = Double.parseDouble(tokens[8]);
                double longShortRatio = Double.parseDouble(tokens[9]);
                double sellBuyRatio = Double.parseDouble(tokens[10]);
                double btcDOM = Double.parseDouble(tokens[11]);


                DataObject object = new DataObject(coin, interval);
                object.setCandle(candle);
                object.setCurrentFundingRate(fundingRate);
                object.setCurrentOpenInterest(openInterest);
                object.setLongShortRatio(longShortRatio);
                object.setBuySellRatio(sellBuyRatio);
                object.setBTCDomination(btcDOM);

                double spx = Double.parseDouble(tokens[12]);
                double dxy = Double.parseDouble(tokens[13]);
                double dji = Double.parseDouble(tokens[14]);
                double vix = Double.parseDouble(tokens[15]);
                double ndx = Double.parseDouble(tokens[16]);
                double gold = Double.parseDouble(tokens[17]);

                FundamentalStockObject fundamentalStock = new FundamentalStockObject(TimeFrameConverter.convert(interval));
                fundamentalStock.setSPX(spx);
                fundamentalStock.setDXY(dxy);
                fundamentalStock.setDJI(dji);
                fundamentalStock.setVIX(vix);
                fundamentalStock.setNDX(ndx);
                fundamentalStock.setGOLD(gold);

                object.setFundamentalData(fundamentalStock);

                double[] sentValues = sentiment.getValueForNearestDate(candle.getOpenTime());
                object.setSentimentMean(sentValues[0]);
                object.setSentimentSum(sentValues[1]);
                localData.add(object);
                candles.add(candle);
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }

        IndicatorsDataUtil util = new IndicatorsDataUtil(candles);
        for (int i = SKIP_NUMBER; i < localData.size(); i++) {
            DataObject object = localData.get(i);
            object.setCurrentIndicators(util.getIndicators(i));
            this.data.add(object);
        }
        isInitialized = true;
    }

    public static void main(String[] args) {
        CSVCoinDataSet dataSet = new CSVCoinDataSet(Coin.BTCUSDT, TimeFrame.FIFTEEN_MINUTES);
        dataSet.load();
        System.out.println(dataSet.data.removeLast());
        System.out.println(dataSet.data.removeLast());
        System.out.println(dataSet.data.removeLast());
    }
}
