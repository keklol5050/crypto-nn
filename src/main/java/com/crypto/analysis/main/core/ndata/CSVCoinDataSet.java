package com.crypto.analysis.main.core.ndata;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.IndicatorsDataUtil;
import com.crypto.analysis.main.core.data_utils.utils.PropertiesUtil;
import com.crypto.analysis.main.core.data_utils.utils.SentimentUtil;
import com.crypto.analysis.main.core.vo.CandleObject;
import com.crypto.analysis.main.core.vo.DataObject;
import com.crypto.analysis.main.core.vo.FundamentalCryptoDataObject;
import com.crypto.analysis.main.core.vo.FundamentalStockObject;
import com.crypto.analysis.main.core.vo.indication.SentimentHistoryObject;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.crypto.analysis.main.core.data_utils.select.StaticUtils.sdfFullISO;
import static com.crypto.analysis.main.core.vo.DataObject.SKIP_NUMBER;

public class CSVCoinDataSet {
    private final Path path;
    private final Coin coin;

    @Getter
    private final TimeFrame tf;
    @Getter
    private final ArrayList<DataObject> data;
    @Getter
    private boolean isInitialized = false;

    private static final Logger logger = LoggerFactory.getLogger(CSVCoinDataSet.class);

    public CSVCoinDataSet(Coin coin, TimeFrame tf) {
        this.coin = coin;
        this.tf = tf;
        this.path = Path.of(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getParent() + PropertiesUtil.getProperty("data.datasets_path") + coin + "/" + tf.getTimeFrame() + ".csv");
        data = new ArrayList<>();
    }

    public void load() {
        if (isInitialized) return;

        logger.info(String.format("Coin %s, Time frame %s", coin, tf));
        logger.info("Loading CSV data from {}", path);

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

                DataObject object = new DataObject(coin, tf);
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
                fundamentalStock.setSPX(new float[]{0,0,0,spx});
                fundamentalStock.setDXY(new float[]{0,0,0,dxy});
                fundamentalStock.setDJI(new float[]{0,0,0,dji});
                fundamentalStock.setVIX(new float[]{0,0,0,vix});
                fundamentalStock.setNDX(new float[]{0,0,0,ndx});
                fundamentalStock.setGOLD(new float[]{0,0,0,gold});
                object.setFundamentalData(fundamentalStock);

                float f1 = Float.parseFloat(tokens[17]);
                float f2 = Float.parseFloat(tokens[18]);
                float f3 = Float.parseFloat(tokens[19]);
                float f4 = Float.parseFloat(tokens[20]);
                float f5 = Float.parseFloat(tokens[21]);
                float f6 = Float.parseFloat(tokens[22]);
                float f7 = Float.parseFloat(tokens[23]);
                float f8 = Float.parseFloat(tokens[24]);

                FundamentalCryptoDataObject fCrypto = new FundamentalCryptoDataObject(coin, new float[]{f1, f2, f3, f4, f5, f6, f7, f8});

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

        logger.info(String.format("Coin %s, Time frame %s loaded", coin, tf));
        isInitialized = true;
    }

    public static void main(String[] args) {
        CSVCoinDataSet set = new CSVCoinDataSet(Coin.BTCUSDT, TimeFrame.FOUR_HOUR);
        set.load();
    }
}
