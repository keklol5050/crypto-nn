package com.crypto.analysis.main.core.data_utils.select;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class StaticUtils {
    public static final UMFuturesClientImpl client = new UMFuturesClientImpl();
    public static final ObjectMapper objectMapper = new ObjectMapper();
    public static final OkHttpClient okHttpClient = new OkHttpClient();

    public static final SimpleDateFormat sdfFullISO = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat sdfShortISO = new SimpleDateFormat("yyyy-MM-dd");
    public static final String defaultZone = "UTC+0";
    static {
        sdfFullISO.setTimeZone(TimeZone.getTimeZone(defaultZone));
        sdfShortISO.setTimeZone(TimeZone.getTimeZone(defaultZone));
        Locale.setDefault(Locale.US);
    }
}
