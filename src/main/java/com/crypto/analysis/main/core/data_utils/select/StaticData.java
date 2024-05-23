package com.crypto.analysis.main.core.data_utils.select;

import ai.djl.Device;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class StaticData {
    public static final UMFuturesClientImpl client = new UMFuturesClientImpl();
    public static final ObjectMapper objectMapper = new ObjectMapper();
    public static final OkHttpClient okHttpClient = new OkHttpClient();
    public static final NDManager manager = NDManager.newBaseManager(Device.gpu());

    public static final int[] MASK_OUTPUT = new int[]{3}; // C
    public static final int POSITION_OF_PRICES_NORMALIZER_IND = 3;
    public static final int SKIP_NUMBER = 800;

    public static final int binanceCapacityMax = 490;

    public static final int MODEL_NUM_INPUTS = 61;
    public static final int BATCH_SIZE = 128;
    public static final int MOVING_AVERAGES_COUNT_FOR_DIFF_WITH_PRICE_VALUES = 28;

    public static final int NUMBER_OF_DIFFERENTIATIONS = 0;
    public static final int COUNT_PRICES_VALUES = 4;
    public static final int COUNT_VALUES_NOT_VOLATILE_WITHOUT_MA = 13;
    public static final int COUNT_VALUES_FOR_DIFFERENTIATION = COUNT_VALUES_NOT_VOLATILE_WITHOUT_MA;

    public static final Device DEVICE = Device.gpu();
    public static final DataType DATA_TYPE = DataType.FLOAT32;


    public static final String ALL_SENTIMENT_DATA = "https://api.senticrypt.com/v2/all.json"; // SentiCrypt, class SentimentUtil
    public static final String bitQueryApiKey = "BQYM7GOqY0AGd3Sss16XsDtijqogxOM6"; // BitQuery crypto fundamental api key, class BitQueryUtil
    public static final String twelveDataKey = "4bb2e7eb9fe24d8d907287b30ae5eb4b"; // TwelveData API fundamental stocks, class FundamentalUtil

    public static final String BITQUERY_HOUR_REQ = "{\"query\":\"{%s {\\ntransactions(date: {since: \\\"%s\\\", till: \\\"%s\\\"}) {\\ncount feeValue fee_avg: feeValue(calculate: average)\\ninputCount inputValue minedValue outputCount\\noutputValue\\nblock {timestamp {year month dayOfMonth hour}}\\n}}}\\n\",\"variables\":\"{}\"}";
    public static final String BITQUERY_SIMPLE_REQ = "{\"query\":\"{%s {\\ntransactions(date: {since: \\\"%s\\\", till: \\\"%s\\\"}) {\\ncount feeValue fee_avg: feeValue(calculate: average) \\ninputCount inputValue minedValue\\noutputCount outputValue\\nblock {timestamp {time}}\\n}}}\\n\",\"variables\":\"{}\"}";


    public static final Path pathToFifteenMinutesBTCDataSet = Path.of("C:/static/datasets/btc/bitcoin_15m.csv");
    public static final Path pathToOneHourBTCDataSet = Path.of("C:/static/datasets/btc/bitcoin_1h.csv");
    public static final Path pathToFourHourBTCDataSet = Path.of("C:/static/datasets/btc/bitcoin_4h.csv");

    public static final Path pathToBTCDOM = Path.of("C:/static/data/btcdom-15m.csv");

    public static final Path pathToDJI = Path.of("C:/static/data/dji-15m.csv");
    public static final Path pathToDXY = Path.of("C:/static/data/dxy-15m.csv");
    public static final Path pathToNDX = Path.of("C:/static/data/ndx-15m.csv");
    public static final Path pathToSPX = Path.of("C:/static/data/spx-15m.csv");
    public static final Path pathToVIX = Path.of("C:/static/data/vix-15m.csv");
    public static final Path pathToGOLD = Path.of("C:/static/data/xau-15m.csv");

    public static final Path pathToBTCFund15m = Path.of("C:/static/data/btc/fund/fund_BTCUSDT_15m.csv");
    public static final Path pathToBTCFund1h = Path.of("C:/static/data/btc/fund/fund_BTCUSDT_1h.csv");
    public static final Path pathToBTCFund4h = Path.of("C:/static/data/btc/fund/fund_BTCUSDT_4h.csv");
    public static final Path pathToBTCFunding = Path.of("C:/static/data/btc/funding-BTCUSDT.csv");
    public static final Path pathToBTCMetrics = Path.of("C:/static/data/btc/metrics-BTCUSDT.csv");
    public static final Path pathToBTCCandles15m = Path.of("C:/static/data/btc/candles/15m-candles.csv");
    public static final Path pathToBTCCandles1h = Path.of("C:/static/data/btc/candles/1h-candles.csv");
    public static final Path pathToBTCCandles4h = Path.of("C:/static/data/btc/candles/4h-candles.csv");

    public static final SimpleDateFormat sdfFullISO = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat sdfShortISO = new SimpleDateFormat("yyyy-MM-dd");


    public static final int COUNT_EPOCHS_TO_SAVE_MODEL = 20;
    public static final String PATH_TO_MODEL = "";

    static {
        sdfFullISO.setTimeZone(TimeZone.getTimeZone("UTC+0"));
        sdfShortISO.setTimeZone(TimeZone.getTimeZone("UTC+0"));
        Locale.setDefault(Locale.US);
    }
    public static void main(String[] args) {
        System.out.println(Math.log(250));
        System.out.println(Math.log(60));
        System.out.println(Math.log(10));
        System.out.println(new Date(1715248800000l));
    }
}
