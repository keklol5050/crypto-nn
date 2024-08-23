package com.crypto.analysis.main.core.strategies;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.MinusDIIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

public class StrategyInitializer {
    public static BaseStrategy getStrategy(BarSeries series, StrategyType type) {
        return switch (type) {
            case SMA -> getSMAStrategy(series);
            case ADX -> getADXStrategy(series);
            case RSI -> getRSIStrategy(series);
            case RSI_STOCHASTIC -> getRSIAndStochasticStrategy(series);
        };
    }
    public static BaseStrategy getSMAStrategy(BarSeries series) {
        SMAIndicator shortSma = new SMAIndicator(new ClosePriceIndicator(series), 50);
        SMAIndicator longSma = new SMAIndicator(new ClosePriceIndicator(series), 200);

        Rule inputRule = new CrossedUpIndicatorRule(shortSma, longSma);
        Rule outputRule = new CrossedDownIndicatorRule(shortSma, longSma);

        return new BaseStrategy(inputRule, outputRule);
    }

    public static BaseStrategy getADXStrategy(BarSeries series) {
        ADXIndicator adx = new ADXIndicator(series, 14);
        PlusDIIndicator plusDI = new PlusDIIndicator(series, 14);
        MinusDIIndicator minusDI = new MinusDIIndicator(series, 14);

        Rule inputRule =  new OverIndicatorRule(adx, 25).and(new CrossedUpIndicatorRule(plusDI, minusDI));
        Rule outputRule = new OverIndicatorRule(adx, 25).and(new CrossedUpIndicatorRule(minusDI, plusDI));

        return new BaseStrategy(inputRule, outputRule);
    }

    public static BaseStrategy getRSIAndStochasticStrategy(BarSeries series) {
        RSIIndicator rsi = new RSIIndicator(new ClosePriceIndicator(series), 14);
        StochasticOscillatorKIndicator stochasticK = new StochasticOscillatorKIndicator(series, 14);

        Rule inputRule = new UnderIndicatorRule(rsi, 30).and(new UnderIndicatorRule(stochasticK, 20));
        Rule outputRule = new OverIndicatorRule(rsi, 70).and(new OverIndicatorRule(stochasticK, 80));

        return new BaseStrategy(inputRule, outputRule);
    }

    public static BaseStrategy getRSIStrategy(BarSeries series) {
        RSIIndicator rsi = new RSIIndicator(new ClosePriceIndicator(series), 14);

        Rule inputRule = new UnderIndicatorRule(rsi, 25);
        Rule outputRule = new OverIndicatorRule(rsi, 75);

        return new BaseStrategy(inputRule, outputRule);
    }
}
