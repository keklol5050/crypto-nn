package com.crypto.analysis.main.core.ndata;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.IndicatorsDataUtil;
import com.crypto.analysis.main.core.data_utils.utils.SentimentUtil;
import com.crypto.analysis.main.core.vo.CandleObject;
import com.crypto.analysis.main.core.vo.DataObject;
import com.crypto.analysis.main.core.vo.FundamentalCryptoDataObject;
import com.crypto.analysis.main.core.vo.FundamentalStockObject;
import com.crypto.analysis.main.core.vo.indication.SentimentHistoryObject;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.crypto.analysis.main.core.data_utils.select.StaticData.*;

public class CSVCoinDataSet {
    private final Path path;
    private final Coin coin;

    @Getter
    private final TimeFrame interval;
    @Getter
    private final ArrayList<DataObject> data;
    @Getter
    private boolean isInitialized = false;

    public CSVCoinDataSet(Coin coin, TimeFrame interval) {
        this.coin = coin;
        this.interval = interval;
        this.path = switch (interval) {
            case FIFTEEN_MINUTES -> pathToFifteenMinutesBTCDataSet;
            case ONE_HOUR -> pathToOneHourBTCDataSet;
            case FOUR_HOUR -> pathToFourHourBTCDataSet;
        };
        data = new ArrayList<>();
    }

    public void load() {
        if (isInitialized) return;
        ArrayList<DataObject> localData = new ArrayList<DataObject>();
        ArrayList<CandleObject> candles = new ArrayList<CandleObject>();

        SentimentHistoryObject sentiment = SentimentUtil.getData();

        try {
            List<String> lines = Files.readAllLines(path);
            lines.removeFirst();
            for (String line : lines) {
                String[] tokens = line.split(",");

                Date openTime = sdfFullISO.parse(tokens[0]);
                double open = Double.parseDouble(tokens[1]);
                double high = Double.parseDouble(tokens[2]);
                double low = Double.parseDouble(tokens[3]);
                double close = Double.parseDouble(tokens[4]);
                double volume = Double.parseDouble(tokens[5]);
                Date closeTime = sdfFullISO.parse(tokens[6]);
                CandleObject candle = new CandleObject(openTime, open, high, low, close, volume, closeTime);

                double fundingRate = Double.parseDouble(tokens[7]);
                double openInterest = Double.parseDouble(tokens[8]);
                double longShortRatio = Double.parseDouble(tokens[9]);
                double btcDOM = Double.parseDouble(tokens[10]);

                DataObject object = new DataObject(coin, interval);
                object.setCandle(candle);
                object.setCurrentFundingRate(fundingRate);
                object.setCurrentOpenInterest(openInterest);
                object.setLongShortRatio(longShortRatio);
                object.setBTCDomination(btcDOM);

                double spx = Double.parseDouble(tokens[11]);
                double dxy = Double.parseDouble(tokens[12]);
                double dji = Double.parseDouble(tokens[13]);
                double vix = Double.parseDouble(tokens[14]);
                double ndx = Double.parseDouble(tokens[15]);
                double gold = Double.parseDouble(tokens[16]);

                FundamentalStockObject fundamentalStock = new FundamentalStockObject();
                fundamentalStock.setSPX(spx);
                fundamentalStock.setDXY(dxy);
                fundamentalStock.setDJI(dji);
                fundamentalStock.setVIX(vix);
                fundamentalStock.setNDX(ndx);
                fundamentalStock.setGOLD(gold);
                object.setFundamentalData(fundamentalStock);

                double transactions_count = Double.parseDouble(tokens[17]);
                double fee_value = Double.parseDouble(tokens[18]);
                double fee_average = Double.parseDouble(tokens[19]);
                double input_count = Double.parseDouble(tokens[20]);
                double input_value = Double.parseDouble(tokens[21]);
                double mined_value = Double.parseDouble(tokens[22]);
                double output_count = Double.parseDouble(tokens[23]);
                double output_value = Double.parseDouble(tokens[24]);

                FundamentalCryptoDataObject fCrypto = new FundamentalCryptoDataObject(coin, new double[]{transactions_count, fee_value, fee_average,
                        input_count, input_value, mined_value, output_count, output_value});

                object.setCryptoFundamental(fCrypto);

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
}
