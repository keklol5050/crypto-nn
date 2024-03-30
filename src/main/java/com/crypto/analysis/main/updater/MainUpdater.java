package com.crypto.analysis.main.updater;

import com.crypto.analysis.main.data_utils.select.coin.Coin;
import com.crypto.analysis.main.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.data_utils.select.fundamental.FundamentalStock;
import com.crypto.analysis.main.fundamental.stock.FundamentalDataUtil;

import java.util.Date;

public class MainUpdater {
    private final Date timestamp;
    public MainUpdater(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void updateData(Coin coin) {
        CoinUpdater coinUpdater = new CoinUpdater(coin);
        coinUpdater.updateFunding();
        coinUpdater.updateMetrics();
        for (TimeFrame tf : TimeFrame.values()) {
            coinUpdater.updateFundamentalData(tf);
        }

        CandlesUpdater candlesUpdater = new CandlesUpdater(coin);
        for (TimeFrame tf : TimeFrame.values()) {
            candlesUpdater.update(tf);
        }
    }

    public void updateData() {
        for (Coin coin : Coin.values()) {
            updateData(coin);
        }
    }

    public void updateFundamental(FundamentalDataUtil fdUtil) {
        FundamentalUpdater fundamentalUpdater = new FundamentalUpdater(fdUtil);
        fundamentalUpdater.updateBTCDOM();
        for (FundamentalStock stock : FundamentalStock.values()) {
            fundamentalUpdater.update(stock);
        }
    }
}
