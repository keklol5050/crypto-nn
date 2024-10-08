package com.crypto.analysis.main.core.data_utils.utils;

import com.crypto.analysis.main.core.vo.indication.SentimentHistoryObject;
import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.TreeMap;

import static com.crypto.analysis.main.core.data_utils.select.StaticUtils.*;

public class SentimentUtil {
    public static final String ALL_SENTIMENT_DATA = PropertiesUtil.getProperty("senticrypt.api_str"); // SentiCrypt, class SentimentUtil

    public static SentimentHistoryObject getData() {

        Request request = new Request.Builder()
                .url(ALL_SENTIMENT_DATA)
                .build();

        TreeMap<Date, float[]> resultMap = new TreeMap<>();

        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {

                assert response.body() != null;
                String jsonData = response.body().string();

                JsonNode valuesNode = objectMapper.readTree(jsonData);

                for (JsonNode node : valuesNode) {
                    Date openTime = sdfShortISO.parse(node.get("date").asText());
                    float mean = (float) node.get("mean").asDouble();
                    float sum = (float) node.get("sum").asDouble();
                    resultMap.put(openTime, new float[]{mean, sum});
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
