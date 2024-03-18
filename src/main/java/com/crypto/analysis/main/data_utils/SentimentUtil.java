package com.crypto.analysis.main.data_utils;

import com.crypto.analysis.main.fundamental.stock.enumerations.FundamentalStock;
import com.crypto.analysis.main.fundamental.stock.enumerations.FundamentalTimeFrame;
import com.crypto.analysis.main.vo.SentimentHistoryObject;
import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.TreeMap;

import static com.crypto.analysis.main.data_utils.BinanceDataUtil.objectMapper;

public class SentimentUtil {
    public static final String ALL_SENTIMENT_DATA = "https://api.senticrypt.com/v2/all.json";
    private static final OkHttpClient client = new OkHttpClient();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    static {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC+0"));
    }

    public static SentimentHistoryObject getData() {

        Request request = new Request.Builder()
                .url(ALL_SENTIMENT_DATA)
                .build();

        TreeMap<Date, double[]> resultMap = new TreeMap<>();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {

                assert response.body() != null;
                String jsonData = response.body().string();

                JsonNode valuesNode = objectMapper.readTree(jsonData);

                for (JsonNode node : valuesNode) {
                    Date openTime = sdf.parse(node.get("date").asText());
                    double mean = node.get("mean").asDouble();
                    double sum = node.get("sum").asDouble();
                    resultMap.put(openTime, new double[]{mean, sum});
                }
            } else {
                System.out.println("Error: " + response.code() + " " + response.message());
            }
            response.close();
        } catch (IOException | ParseException e) {
            e.printStackTrace(System.out);
        }

        return new SentimentHistoryObject(resultMap);
    }
}
