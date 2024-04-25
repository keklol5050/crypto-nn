package com.crypto.analysis.main;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.IndicatorsDataUtil;
import com.crypto.analysis.main.core.data_utils.utils.BinanceDataUtil;
import org.ta4j.core.*;
import org.ta4j.core.criteria.pnl.*;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.rules.*;

import java.io.IOException;

public class CrossStrategy {

    public static void main(String[] args) throws IOException {

        BarSeries series = IndicatorsDataUtil.getTimeSeries(BinanceDataUtil.getCandles(Coin.BTCUSDT, TimeFrame.FIFTEEN_MINUTES, 1500));

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator shortSma = new SMAIndicator(closePrice, 20); // Короткая скользящая средняя
        EMAIndicator longEma = new EMAIndicator(closePrice, 50); // Длинная экспоненциальная скользящая средняя
        StochasticOscillatorKIndicator stochasticK = new StochasticOscillatorKIndicator(series, 14); // Стохастический осциллятор %K
        StochasticOscillatorDIndicator stochasticD = new StochasticOscillatorDIndicator(stochasticK); // Стохастический осциллятор %D

        // Создайте правила входа и выхода
        Rule entryRule = new CrossedUpIndicatorRule(shortSma, longEma) // Вход, когда короткая СС пересекает длинную сверху
                .and(new OverIndicatorRule(stochasticK, 20)) // и %K стохастического осциллятора выше 20
                .and(new OverIndicatorRule(stochasticD, 20)); // и %D стохастического осциллятора выше 20

        Rule exitRule = new CrossedDownIndicatorRule(shortSma, longEma) // Выход, когда короткая СС пересекает длинную снизу
                .or(new UnderIndicatorRule(stochasticK, 80)) // или %K стохастического осциллятора ниже 80
                .or(new UnderIndicatorRule(stochasticD, 80)) // или %D стохастического осциллятора ниже 80
                .or(new StopLossRule(closePrice, 0.95)) // или достигнут уровень стоп-лосса (5% ниже входной цены)
                .or(new StopGainRule(closePrice, 1.1)); // или достигнут уровень профит-тейка (10% выше входной цены)

        // Создайте стратегию
        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        // Создайте стратегию
        BarSeriesManager manager = new BarSeriesManager(series);
        TradingRecord tradingRecord = manager.run(strategy, Trade.TradeType.BUY, DecimalNum.valueOf(0.0002));
        System.out.println("Number of trades for the strategy: " + tradingRecord.getPositionCount());

        // Analysis
        System.out.println("Total profit for the strategy: " + new GrossReturnCriterion().calculate(series, tradingRecord));
    }
}