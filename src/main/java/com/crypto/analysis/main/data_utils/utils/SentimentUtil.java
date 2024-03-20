package com.crypto.analysis.main.data_utils.utils;

import com.crypto.analysis.main.vo.indication.SentimentHistoryObject;
import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.TreeMap;

import static com.crypto.analysis.main.data_utils.select.StaticData.*;

public class SentimentUtil {

    public static SentimentHistoryObject getData() {

        Request request = new Request.Builder()
                .url(ALL_SENTIMENT_DATA)
                .build();

        TreeMap<Date, double[]> resultMap = new TreeMap<>();

        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {

                assert response.body() != null;
                String jsonData = response.body().string();

                JsonNode valuesNode = objectMapper.readTree(jsonData);

                for (JsonNode node : valuesNode) {
                    Date openTime = sdfShortISO.parse(node.get("date").asText());
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
