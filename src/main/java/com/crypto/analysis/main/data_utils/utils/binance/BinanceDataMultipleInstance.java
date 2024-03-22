package com.crypto.analysis.main.data_utils.utils.binance;

import com.crypto.analysis.main.data_utils.select.coin.Coin;
import com.crypto.analysis.main.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.data_utils.utils.IndicatorsDataUtil;
import com.crypto.analysis.main.data_utils.utils.SentimentUtil;
import com.crypto.analysis.main.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.vo.CandleObject;
import com.crypto.analysis.main.vo.DataObject;
import com.crypto.analysis.main.vo.indication.*;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

public class BinanceDataMultipleInstance {
    public static DataObject[] getLatestInstances(Coin coin, TimeFrame interval, int count, FundamentalDataUtil fundUtil) {
        DataObject[] instances = new DataObject[count];
        LinkedList<CandleObject> candles = BinanceDataUtil.getCandles(coin, interval, count+5);

        IndicatorsDataUtil util = new IndicatorsDataUtil(coin, interval);
        FundingHistoryObject funding = BinanceDataUtil.getFundingHistory(coin);

        LongShortRatioHistoryObject longShortRatioHistoryObject = BinanceDataUtil.getLongShortRatio(coin, interval);
        OpenInterestHistoryObject openInterest = BinanceDataUtil.getOpenInterest(coin, interval);
        BuySellRatioHistoryObject buySellRatio = BinanceDataUtil.getBuySellRatio(coin, interval);

        BTCDOMObject BTCDom = BinanceDataUtil.getBTCDomination(interval);

        int countDeleted = 0;
        while (true) {
            boolean isAllAvailable = true;
            Date lastDate = candles.getLast().getOpenTime();

            if (!longShortRatioHistoryObject.contains(lastDate)) isAllAvailable=false;
            if (!openInterest.contains(lastDate)) isAllAvailable=false;
            if (!buySellRatio.contains(lastDate)) isAllAvailable=false;
            if (isAllAvailable) break;

            candles.removeLast();
            countDeleted++;
        }

        while (candles.size()!=count) candles.removeFirst();

        SentimentHistoryObject sentiment = SentimentUtil.getData();

        for (int i = 0; i < count; i++) {
            DataObject obj = new DataObject(coin, interval);
            obj.setCurrentIndicators(util.getIndicators(candles.size() + countDeleted - 1));

            CandleObject candle = candles.removeFirst();
            obj.setCandle(candle);

            obj.setCurrentFundingRate(funding.getValueForNearestDate(candle.getOpenTime()));
            obj.setCurrentOpenInterest(openInterest.getValueForNearestDate(candle.getOpenTime()));
            obj.setLongShortRatio(longShortRatioHistoryObject.getValueForNearestDate(candle.getOpenTime()));
            obj.setBuySellRatio(buySellRatio.getValueForNearestDate(candle.getOpenTime()));

            obj.setBTCDomination(BTCDom.getValueForNearestDate(candle.getOpenTime()));
            obj.setFundamentalData(fundUtil.getFundamentalData(candle));

            double[] sentValues = sentiment.getValueForNearestDate(candle.getOpenTime());
            obj.setSentimentMean(sentValues[0]);
            obj.setSentimentSum(sentValues[1]);

            instances[i] = obj;
        }

        return instances;
    }

    public static void main(String[] args) {
        System.out.println(
                Arrays.toString(BinanceDataMultipleInstance.getLatestInstances(Coin.BTCUSDT, TimeFrame.ONE_HOUR, 10, new FundamentalDataUtil())));
    }
}
