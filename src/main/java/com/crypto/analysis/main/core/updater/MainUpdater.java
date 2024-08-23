package com.crypto.analysis.main.core.updater;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.select.fundamental.FundamentalStock;
import com.crypto.analysis.main.core.fundamental.stock.FundamentalDataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

public class MainUpdater {
    private final Date timestamp;

    private static final Logger logger = LoggerFactory.getLogger(MainUpdater.class);
    public MainUpdater() {
        this.timestamp = new Date();
        logger.info(String.format("Updater started at %s", timestamp));
    }

    public void updateCoinData(Coin coin) {
        CoinUpdater coinUpdater = new CoinUpdater(coin);
        coinUpdater.updateFunding();
        logger.info("Funding updated");

        coinUpdater.updateMetrics();
        logger.info("Metrics updated");

        for (TimeFrame tf : TimeFrame.values()){
            coinUpdater.updateFundamentalData(tf);
        }
        logger.info("Coin fundamental data updated");

        updateCandles(coin);
    }

    public void updateCandles(Coin coin) {
        CandlesUpdater candlesUpdater = new CandlesUpdater(coin);
        for (TimeFrame tf : TimeFrame.values()){
            candlesUpdater.update(tf);
        }
        logger.info("Candles data updated");
    }

    public void updateFundamental(FundamentalDataUtil fdUtil) {
        FundamentalUpdater fundamentalUpdater = new FundamentalUpdater(fdUtil);
        fundamentalUpdater.updateBTCDOM();
        for (FundamentalStock stock : FundamentalStock.values()) {
            fundamentalUpdater.update(stock);
        }
        logger.info("Fundamental data updated");
    }

    public void updateDataSet(Coin coin) {
        DataSetUpdater updater = DataSetUpdater.getInstance();
        try {
            for (TimeFrame tf : TimeFrame.values())
                updater.update(coin, tf);
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
        logger.info(String.format("Data sets updated for %s", coin));
    }

    public void updateData(FundamentalDataUtil fdUtil) {
        for (Coin coin : Coin.values()) {
            updateCoinData(coin);
        }
        updateFundamental(fdUtil);
        logger.info("Data updated successfully");
    }
    public void updateDataAndDatasets(FundamentalDataUtil fdUtil) {
        updateData(fdUtil);
        for (Coin coin : Coin.values()) {
            updateDataSet(coin);
        }
    }

    public static void main(String[] args) {
        MainUpdater m = new MainUpdater();
        m.updateData(new FundamentalDataUtil());
    }
}
