package com.crypto.analysis.main.core.data_utils.utils.binance;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.SentimentUtil;
import com.crypto.analysis.main.core.fundamental.crypto.BitQueryUtil;
import com.crypto.analysis.main.core.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.core.vo.DataObject;
import com.crypto.analysis.main.core.vo.indication.*;
import com.crypto.analysis.main.core.data_utils.utils.IndicatorsDataUtil;
import com.crypto.analysis.main.core.vo.CandleObject;

import java.util.Arrays;
import java.util.LinkedList;

public class BinanceDataMultipleInstance {
    public static DataObject[] getLatestInstances(Coin coin, TimeFrame interval, int count, FundamentalDataUtil fundUtil) {
        DataObject[] instances = new DataObject[count];
        LinkedList<CandleObject> candles = BinanceDataUtil.getCandles(coin, interval, count);

        IndicatorsDataUtil util = new IndicatorsDataUtil(coin, interval);
        FundingHistoryObject funding = BinanceDataUtil.getFundingHistory(coin);

        LongShortRatioHistoryObject longShortRatioHistoryObject = BinanceDataUtil.getLongShortRatio(coin, interval);
        OpenInterestHistoryObject openInterest = BinanceDataUtil.getOpenInterest(coin, interval);

        BTCDOMObject BTCDom = BinanceDataUtil.getBTCDomination(interval);
        SentimentHistoryObject sentiment = SentimentUtil.getData();

        BitQueryUtil bitQueryUtil = new BitQueryUtil(coin, interval);
        bitQueryUtil.initData(candles.getFirst().getOpenTime(), candles.getLast().getCloseTime());

        for (int i = 0; i < count; i++) {
            DataObject obj = new DataObject(coin, interval);
            obj.setCurrentIndicators(util.getIndicators(candles.size() - 1));

            CandleObject candle = candles.removeFirst();
            obj.setCandle(candle);

            obj.setCurrentFundingRate(funding.getValueForNearestDate(candle.getOpenTime()));
            obj.setCurrentOpenInterest(openInterest.getValueForNearestDate(candle.getOpenTime()));
            obj.setLongShortRatio(longShortRatioHistoryObject.getValueForNearestDate(candle.getOpenTime()));

            obj.setBTCDomination(BTCDom.getValueForNearestDate(candle.getOpenTime()));
            obj.setFundamentalData(fundUtil.getFundamentalData(candle));

            double[] sentValues = sentiment.getValueForNearestDate(candle.getOpenTime());
            obj.setSentimentMean(sentValues[0]);
            obj.setSentimentSum(sentValues[1]);

            obj.setCryptoFundamental(bitQueryUtil.getData(candle));

            instances[i] = obj;
        }

        return instances;
    }

    public static void main(String[] args) {
        System.out.println(
                Arrays.toString(BinanceDataMultipleInstance.getLatestInstances(Coin.BTCUSDT, TimeFrame.ONE_HOUR, 10, new FundamentalDataUtil())));
    }
}
