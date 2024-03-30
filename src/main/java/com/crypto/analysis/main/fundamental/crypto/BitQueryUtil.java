package com.crypto.analysis.main.fundamental.crypto;

import com.crypto.analysis.main.data_utils.select.StaticData;
import com.crypto.analysis.main.data_utils.select.coin.Coin;
import com.crypto.analysis.main.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.vo.CandleObject;
import com.crypto.analysis.main.vo.FundamentalCryptoDataObject;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static com.crypto.analysis.main.data_utils.select.StaticData.*;

public class BitQueryUtil {
    private final Coin coin;
    private final TimeFrame interval;

    @Getter
    private TreeMap<Date, double[]> data;

    public BitQueryUtil(Coin coin, TimeFrame interval) {
        this.coin = coin;
        this.interval = interval;
    }

    public static void main(String[] args) {
        new BitQueryUtil(Coin.BTCUSDT, TimeFrame.FOUR_HOUR).initData(new Date(124, 02, 20), new Date());
    }

    public FundamentalCryptoDataObject getData(CandleObject candle) {
        double[] in = data.floorEntry(candle.getOpenTime()).getValue();
        return new FundamentalCryptoDataObject(coin, in);
    }

    public void initData(Date start, Date end) {
        MediaType mediaType = MediaType.parse("application/json");

        String coin = switch (this.coin) {
            case BTCUSDT -> "bitcoin";
            default -> throw new IllegalArgumentException();
        };
        String startDate = sdfShortISO.format(start);
        String endDate = sdfShortISO.format(end);

        RequestBody body = RequestBody.create(mediaType, switch (interval) {
            case ONE_HOUR, FOUR_HOUR -> String.format(BITQUERY_HOUR_REQ, coin, startDate, endDate);
            default -> String.format(BITQUERY_SIMPLE_REQ, coin, startDate, endDate);
        });
        Request request = new Request.Builder()
                .url("https://graphql.bitquery.io")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-API-KEY", bitQueryApiKey)
                .build();
        TreeMap<Date, double[]> result = null;
        try {
            TreeMap<Date, double[]> temp = new TreeMap<>();
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {

                assert response.body() != null;
                String jsonData = response.body().string();

                JsonNode valuesNode = objectMapper.readTree(jsonData)
                        .get("data")
                        .get("bitcoin")
                        .get("transactions");

                for (JsonNode node : valuesNode) {
                    double count = node.get("count").asDouble();

                    double feeValue = node.get("feeValue").asDouble();
                    double feeAvg = node.get("fee_avg").asDouble();

                    double inputCount = node.get("inputCount").asDouble();
                    double inputValue = node.get("inputValue").asDouble();

                    double minedValue = node.get("minedValue").asDouble();

                    double outputCount = node.get("outputCount").asDouble();
                    double outputValue = node.get("outputValue").asDouble();

                    Date timestamp;
                    if (interval == TimeFrame.ONE_HOUR || interval == TimeFrame.FOUR_HOUR) {
                        int year = node.get("block").get("timestamp").get("year").asInt();
                        int month = node.get("block").get("timestamp").get("month").asInt();
                        int day = node.get("block").get("timestamp").get("dayOfMonth").asInt();
                        int hour = node.get("block").get("timestamp").get("hour").asInt();
                        timestamp = sdfFullISO.parse(String.format("%d-%d-%d %d:00:00", year, month, day, hour));
                    } else {
                        timestamp = sdfFullISO.parse(node.get("block").get("timestamp").get("time").asText());
                    }

                    temp.put(timestamp, new double[]{count, feeValue, feeAvg, inputCount, inputValue, minedValue, outputCount, outputValue});
                }

                result = mergeData(temp, interval.getMinuteCount());
            } else {
                throw new RuntimeException("Error: " + response.code() + " " + response.message());
            }
            response.close();
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
        data = result;
    }

    private Date roundDown(Date date, int intervalInMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        int minuteOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        calendar.add(Calendar.MINUTE, -(minuteOfDay % intervalInMinutes));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private TreeMap<Date, double[]> mergeData(TreeMap<Date, double[]> rawData, int interval) {
        TreeMap<Date, double[]> mergedData = new TreeMap<>();

        Date currentIntervalStart = null;
        double[] sumValues = new double[rawData.firstEntry().getValue().length];
        int count = 0;

        for (Map.Entry<Date, double[]> entry : rawData.entrySet()) {
            Date timestamp = entry.getKey();
            double[] values = entry.getValue();

            Date roundedDate = roundDown(timestamp, interval);

            if (currentIntervalStart != null && !currentIntervalStart.equals(roundedDate)) {
                sumValues[2] = sumValues[1] / sumValues[0];
                mergedData.put(currentIntervalStart, sumValues);

                sumValues = new double[values.length];
                count = 0;
            }

            for (int i = 0; i < values.length; i++) {
                sumValues[i] += values[i];
            }
            count++;

            currentIntervalStart = roundedDate;
        }

        if (currentIntervalStart != null) {
            sumValues[2] = sumValues[1] / sumValues[0];
            mergedData.put(currentIntervalStart, sumValues);
        }

        return mergedData;
    }

}
