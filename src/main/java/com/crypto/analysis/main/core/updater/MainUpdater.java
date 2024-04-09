package com.crypto.analysis.main.core.updater;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.select.fundamental.FundamentalStock;
import com.crypto.analysis.main.core.fundamental.stock.FundamentalDataUtil;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

public class MainUpdater {
    private final Date timestamp;

    public MainUpdater() {
        this.timestamp = new Date();
    }

    public void updateCoinData(Coin coin) {
        CoinUpdater coinUpdater = new CoinUpdater(coin);
        coinUpdater.updateFunding();
        coinUpdater.updateMetrics();

        coinUpdater.updateFundamentalData(TimeFrame.FIFTEEN_MINUTES);
        coinUpdater.updateFundamentalData(TimeFrame.ONE_HOUR);
        coinUpdater.updateFundamentalData(TimeFrame.FOUR_HOUR);

        updateCandles(coin);
    }

    public void updateCandles(Coin coin) {
        CandlesUpdater candlesUpdater = new CandlesUpdater(coin);
        candlesUpdater.update(TimeFrame.FIFTEEN_MINUTES);
        candlesUpdater.update(TimeFrame.ONE_HOUR);
        candlesUpdater.update(TimeFrame.FOUR_HOUR);
    }

    public void updateData(FundamentalDataUtil fdUtil) {
        for (Coin coin : Coin.values()) {
            if (coin != Coin.BTCUSDT) continue;
            updateCoinData(coin);
        }
        updateFundamental(fdUtil);
        for (Coin coin : Coin.values()) {
            if (coin != Coin.BTCUSDT) continue;
            updateDataSet(coin);
        }
        System.out.println("Data updated successfully");
    }

    public void updateFundamental(FundamentalDataUtil fdUtil) {
        FundamentalUpdater fundamentalUpdater = new FundamentalUpdater(fdUtil);
        fundamentalUpdater.updateBTCDOM();
        for (FundamentalStock stock : FundamentalStock.values()) {
            fundamentalUpdater.update(stock);
        }
    }

    public void updateDataSet(Coin coin) {
        DataSetUpdater updater = new DataSetUpdater();
        try {
            for (TimeFrame tf : TimeFrame.values())
                updater.update(coin, tf);
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) {
        new MainUpdater().updateData(new FundamentalDataUtil());
    }
}
