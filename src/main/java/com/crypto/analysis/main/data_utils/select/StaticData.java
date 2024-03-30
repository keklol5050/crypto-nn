package com.crypto.analysis.main.data_utils.select;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.crypto.analysis.main.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.ndata.CSVCoinDataSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Objects;
import java.util.TimeZone;

public class StaticData {
    public static final UMFuturesClientImpl client = new UMFuturesClientImpl();
    public static final ObjectMapper objectMapper = new ObjectMapper();
    public static final OkHttpClient okHttpClient = new OkHttpClient();

    public static final int[] MASK_OUTPUT = new int[]{3, 1, 2}; // CHL
    public static final int VOLATILE_VALUES_COUNT_FROM_LAST = 13;
    public static final int SKIP_NUMBER = 800;

    public static final int binanceCapacityMax = 490;

    public static final int MODEL_NUM_INPUTS = 67;
    public static final int MODEL_NUM_OUTPUTS = MASK_OUTPUT.length;
    public static final double MODEL_LEARNING_RATE = 0.005;

    public static final String ALL_SENTIMENT_DATA = "https://api.senticrypt.com/v2/all.json"; // SentiCrypt, class SentimentUtil
    public static final String bitQueryApiKey = "BQYM7GOqY0AGd3Sss16XsDtijqogxOM6"; // BitQuery crypto fundamental api key, class BitQueryUtil
    public static final String twelveDataKey = "4bb2e7eb9fe24d8d907287b30ae5eb4b"; // TwelveData API fundamental stocks, class FundamentalUtil

    public static final String BITQUERY_HOUR_REQ = "{\"query\":\"{%s {\\ntransactions(date: {since: \\\"%s\\\", till: \\\"%s\\\"}) {\\ncount feeValue fee_avg: feeValue(calculate: average)\\ninputCount inputValue minedValue outputCount\\noutputValue\\nblock {timestamp {year month dayOfMonth hour}}\\n}}}\\n\",\"variables\":\"{}\"}";
    public static final String BITQUERY_SIMPLE_REQ = "{\"query\":\"{%s {\\ntransactions(date: {since: \\\"%s\\\", till: \\\"%s\\\"}) {\\ncount feeValue fee_avg: feeValue(calculate: average) \\ninputCount inputValue minedValue\\noutputCount outputValue\\nblock {timestamp {time}}\\n}}}\\n\",\"variables\":\"{}\"}";


    public static final Path pathToFifteenMinutesBTCDataSet = new File(Objects.requireNonNull(CSVCoinDataSet.class.getClassLoader().getResource("static/datasets/bitcoin_15m.csv")).getFile()).toPath();
    public static final Path pathToHourBTCDataSet = new File(Objects.requireNonNull(CSVCoinDataSet.class.getClassLoader().getResource("static/datasets/bitcoin_1h.csv")).getFile()).toPath();
    public static final Path pathToFourHourBTCDataSet = new File(Objects.requireNonNull(CSVCoinDataSet.class.getClassLoader().getResource("static/datasets/bitcoin_4h.csv")).getFile()).toPath();

    public static final Path pathToBTCDOM= new File(Objects.requireNonNull(CSVCoinDataSet.class.getClassLoader().getResource("static/data/btcdom-15m.csv")).getFile()).toPath();

    public static final Path pathToDJI= new File(Objects.requireNonNull(CSVCoinDataSet.class.getClassLoader().getResource("static/data/dji-15m.csv")).getFile()).toPath();
    public static final Path pathToDXY= new File(Objects.requireNonNull(CSVCoinDataSet.class.getClassLoader().getResource("static/data/dxy-15m.csv")).getFile()).toPath();
    public static final Path pathToNDX= new File(Objects.requireNonNull(CSVCoinDataSet.class.getClassLoader().getResource("static/data/ndx-15m.csv")).getFile()).toPath();
    public static final Path pathToSPX= new File(Objects.requireNonNull(CSVCoinDataSet.class.getClassLoader().getResource("static/data/spx-15m.csv")).getFile()).toPath();
    public static final Path pathToVIX= new File(Objects.requireNonNull(CSVCoinDataSet.class.getClassLoader().getResource("static/data/vix-15m.csv")).getFile()).toPath();
    public static final Path pathToGOLD= new File(Objects.requireNonNull(CSVCoinDataSet.class.getClassLoader().getResource("static/data/xau-15m.csv")).getFile()).toPath();

    public static final Path pathToBTCFund15m = new File(Objects.requireNonNull(CSVCoinDataSet.class.getClassLoader().getResource("static/data/btc/fund/fund_BTCUSDT_15m.csv")).getFile()).toPath();
    public static final Path pathToBTCFund1h = new File(Objects.requireNonNull(CSVCoinDataSet.class.getClassLoader().getResource("static/data/btc/fund/fund_BTCUSDT_1h.csv")).getFile()).toPath();
    public static final Path pathToBTCFund4h = new File(Objects.requireNonNull(CSVCoinDataSet.class.getClassLoader().getResource("static/data/btc/fund/fund_BTCUSDT_4h.csv")).getFile()).toPath();
    public static final Path pathToBTCFunding = new File(Objects.requireNonNull(CSVCoinDataSet.class.getClassLoader().getResource("static/data/btc/funding-BTCUSDT.csv")).getFile()).toPath();
    public static final Path pathToBTCMetrics = new File(Objects.requireNonNull(CSVCoinDataSet.class.getClassLoader().getResource("static/data/btc/metrics-BTCUSDT.csv")).getFile()).toPath();
    public static final Path pathToBTCCandles15m = new File(Objects.requireNonNull(CSVCoinDataSet.class.getClassLoader().getResource("static/data/btc/candles/15m-candles.csv")).getFile()).toPath();
    public static final Path pathToBTCCandles1h = new File(Objects.requireNonNull(CSVCoinDataSet.class.getClassLoader().getResource("static/data/btc/candles/1h-candles.csv")).getFile()).toPath();
    public static final Path pathToBTCCandles4h = new File(Objects.requireNonNull(CSVCoinDataSet.class.getClassLoader().getResource("static/data/btc/candles/4h-candles.csv")).getFile()).toPath();

    public static final SimpleDateFormat sdfFullISO = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat sdfShortISO = new SimpleDateFormat("yyyy-MM-dd");

    static {
        sdfFullISO.setTimeZone(TimeZone.getTimeZone("UTC+0"));
    }

    static {
        sdfShortISO.setTimeZone(TimeZone.getTimeZone("UTC+0"));
    }

    public static final LocalDate START_DATE = LocalDate.of(2022, 6, 1);

    public static int getDelimiterForSet(TimeFrame tf) {
        return switch (tf) {
            case FIFTEEN_MINUTES -> 5;
            case ONE_HOUR -> 4;
            case FOUR_HOUR -> 3;
            default -> throw new IllegalStateException("Unexpected value: " + tf);
        };
    }

    public static int getDelimiterForBinance(TimeFrame tf) {
        return 3;
    }

}
