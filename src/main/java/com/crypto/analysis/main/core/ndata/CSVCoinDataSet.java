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
                float open = Float.parseFloat(tokens[1]);
                float high = Float.parseFloat(tokens[2]);
                float low = Float.parseFloat(tokens[3]);
                float close = Float.parseFloat(tokens[4]);
                float volume = Float.parseFloat(tokens[5]);
                Date closeTime = sdfFullISO.parse(tokens[6]);
                CandleObject candle = new CandleObject(openTime, open, high, low, close, volume, closeTime);

                float fundingRate = Float.parseFloat(tokens[7]);
                float openInterest = Float.parseFloat(tokens[8]);
                float longShortRatio = Float.parseFloat(tokens[9]);
                float btcDOM = Float.parseFloat(tokens[10]);

                DataObject object = new DataObject(coin, interval);
                object.setCandle(candle);
                object.setCurrentFundingRate(fundingRate);
                object.setCurrentOpenInterest(openInterest);
                object.setLongShortRatio(longShortRatio);
                object.setBTCDomination(btcDOM);

                float spx = Float.parseFloat(tokens[11]);
                float dxy = Float.parseFloat(tokens[12]);
                float dji = Float.parseFloat(tokens[13]);
                float vix = Float.parseFloat(tokens[14]);
                float ndx = Float.parseFloat(tokens[15]);
                float gold = Float.parseFloat(tokens[16]);

                FundamentalStockObject fundamentalStock = new FundamentalStockObject();
                fundamentalStock.setSPX(spx);
                fundamentalStock.setDXY(dxy);
                fundamentalStock.setDJI(dji);
                fundamentalStock.setVIX(vix);
                fundamentalStock.setNDX(ndx);
                fundamentalStock.setGOLD(gold);
                object.setFundamentalData(fundamentalStock);

                float transactions_count = Float.parseFloat(tokens[17]);
                float fee_value = Float.parseFloat(tokens[18]);
                float fee_average = Float.parseFloat(tokens[19]);
                float input_count = Float.parseFloat(tokens[20]);
                float input_value = Float.parseFloat(tokens[21]);
                float mined_value = Float.parseFloat(tokens[22]);
                float output_count = Float.parseFloat(tokens[23]);
                float output_value = Float.parseFloat(tokens[24]);

                FundamentalCryptoDataObject fCrypto = new FundamentalCryptoDataObject(coin, new float[]{transactions_count, fee_value, fee_average,
                        input_count, input_value, mined_value, output_count, output_value});

                object.setCryptoFundamental(fCrypto);

                float[] sentValues = sentiment.getValueForNearestDate(candle.getOpenTime());
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
