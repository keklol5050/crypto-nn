package com.crypto.analysis.main.data_utils.select;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class StaticData {
    public static final UMFuturesClientImpl client = new UMFuturesClientImpl();
    public static final ObjectMapper objectMapper = new ObjectMapper();
    public static final OkHttpClient okHttpClient = new OkHttpClient();

    public static final int[] MASK_OUTPUT = new int[] {1,2,3}; // HLC
    public static final int VOLATILE_VALUES_COUNT_FROM_LAST = 9;
    public static final int SKIP_NUMBER = 1000;

    public static final String ALL_SENTIMENT_DATA = "https://api.senticrypt.com/v2/all.json"; // SentiCrypt, class SentimentUtil
    public static final String twelveDataKey = "4bb2e7eb9fe24d8d907287b30ae5eb4b"; // TwelveData API fundamental stocks, class FundamentalUtil

    public static final SimpleDateFormat sdfFullISO = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static {
        sdfFullISO.setTimeZone(TimeZone.getTimeZone("UTC+0"));
    }

    public static final SimpleDateFormat sdfShortISO = new SimpleDateFormat("yyyy-MM-dd");
    static {
        sdfShortISO.setTimeZone(TimeZone.getTimeZone("UTC+0"));
    }
}
