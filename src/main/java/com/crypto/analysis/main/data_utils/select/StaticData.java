package com.crypto.analysis.main.data_utils.select;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.crypto.analysis.main.data_utils.select.coin.TimeFrame;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class StaticData {
    public static final UMFuturesClientImpl client = new UMFuturesClientImpl();
    public static final ObjectMapper objectMapper = new ObjectMapper();
    public static final OkHttpClient okHttpClient = new OkHttpClient();

    public static final int[] MASK_OUTPUT = new int[] {3,1,2}; // CHL
    public static final int VOLATILE_VALUES_COUNT_FROM_LAST = 13;
    public static final int SKIP_NUMBER = 800;

    public static final int MODEL_NUM_INPUTS = 67;
    public static final int MODEL_NUM_OUTPUTS = MASK_OUTPUT.length;
    public static final double MODEL_LEARNING_RATE = 0.005;

    public static final String ALL_SENTIMENT_DATA = "https://api.senticrypt.com/v2/all.json"; // SentiCrypt, class SentimentUtil
    public static final String bitQueryApiKey = "BQYM7GOqY0AGd3Sss16XsDtijqogxOM6"; // BitQuery crypto fundamental api key, class BitQueryUtil
    public static final String twelveDataKey = "4bb2e7eb9fe24d8d907287b30ae5eb4b"; // TwelveData API fundamental stocks, class FundamentalUtil

    public static final SimpleDateFormat sdfFullISO = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static {
        sdfFullISO.setTimeZone(TimeZone.getTimeZone("UTC+0"));
    }

    public static final SimpleDateFormat sdfShortISO = new SimpleDateFormat("yyyy-MM-dd");
    static {
        sdfShortISO.setTimeZone(TimeZone.getTimeZone("UTC+0"));
    }

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
