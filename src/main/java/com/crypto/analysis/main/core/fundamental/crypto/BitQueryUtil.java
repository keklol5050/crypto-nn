package com.crypto.analysis.main.core.fundamental.crypto;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.PropertiesUtil;
import com.crypto.analysis.main.core.vo.CandleObject;
import com.crypto.analysis.main.core.vo.FundamentalCryptoDataObject;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static com.crypto.analysis.main.core.data_utils.select.StaticUtils.*;

public class BitQueryUtil {
    public static final String BITQUERY_HOUR_REQ_BTC = "{\"query\":\"{bitcoin {\\ntransactions(date: {since: \\\"%s\\\", till: \\\"%s\\\"}) {\\ncount feeValue fee_avg: feeValue(calculate: average)\\ninputCount inputValue minedValue outputCount\\noutputValue\\nblock {timestamp {year month dayOfMonth hour}}\\n}}}\\n\",\"variables\":\"{}\"}";
    public static final String BITQUERY_SIMPLE_REQ_BTC = "{\"query\":\"{bitcoin {\\ntransactions(date: {since: \\\"%s\\\", till: \\\"%s\\\"}) {\\ncount feeValue fee_avg: feeValue(calculate: average) \\ninputCount inputValue minedValue\\noutputCount outputValue\\nblock {timestamp {time}}\\n}}}\\n\",\"variables\":\"{}\"}";

    public static final String BITQUERY_SIMPLE_REQ_ETH = "{\"query\":\" {ethereum {\\ntransactions(date: {since: \\\"%s\\\", till: \\\"%s\\\"}) {\\nblock {\\ntimestamp {\\nyear\\nmonth\\ndayOfMonth\\nhour\\nminute\\n}\\n}\\ngas\\ngasPrice\\ngasValue\\namount\\ncount\\n}\\n}\\n}\",\"variables\":\"{}\"}";
    public static final String BITQUERY_HOUR_REQ_ETH = "{\"query\":\" {ethereum {\\ntransactions(date: {since: \\\"%s\\\", till: \\\"%s\\\"}) {\\nblock {\\ntimestamp {\\nyear\\nmonth\\ndayOfMonth\\nhour\\n}\\n}\\ngas\\ngasPrice\\ngasValue\\namount\\ncount\\n}\\n}\\n}\",\"variables\":\"{}\"}";

    public static final String bitQueryApiKey = PropertiesUtil.getProperty("bitquery.key");

    private final Coin coin;
    private final TimeFrame interval;

    @Getter
    private TreeMap<Date, float[]> data;

    public BitQueryUtil(Coin coin, TimeFrame interval) {
        this.coin = coin;
        this.interval = interval;
    }

    public static void main(String[] args) {
        new BitQueryUtil(Coin.ETHUSDT, TimeFrame.FOUR_HOUR).initData(new Date(124, 06, 26), new Date());
    }

    public FundamentalCryptoDataObject getData(CandleObject candle) {
        float[] in = data.floorEntry(candle.getOpenTime()).getValue();
        return new FundamentalCryptoDataObject(coin, in);
    }

    public void initData(Date start, Date end) {
        MediaType mediaType = MediaType.parse("application/json");

        String[] coinFundCols = coin.getFundCols();

        String startDate = sdfShortISO.format(start);
        String endDate = sdfShortISO.format(end);

        String requestBodyContent = switch (coin) {
            case Coin.BTCUSDT -> switch (interval) {
                case ONE_HOUR, FOUR_HOUR -> String.format(BITQUERY_HOUR_REQ_BTC, startDate, endDate);
                default -> String.format(BITQUERY_SIMPLE_REQ_BTC, startDate, endDate);
            };
            case Coin.ETHUSDT -> switch (interval) {
                case ONE_HOUR, FOUR_HOUR -> String.format(BITQUERY_HOUR_REQ_ETH, startDate, endDate);
                default -> String.format(BITQUERY_SIMPLE_REQ_ETH, startDate, endDate);
            };
            default -> throw new IllegalStateException("Unexpected value: " + coin);
        };

        RequestBody body = RequestBody.create(mediaType, requestBodyContent);;

        Request request = new Request.Builder()
                .url("https://graphql.bitquery.io")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-API-KEY", bitQueryApiKey)
                .build();
        TreeMap<Date, float[]> result = null;
        try {
            TreeMap<Date, float[]> temp = new TreeMap<>();
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {

                assert response.body() != null;
                String jsonData = response.body().string();

                JsonNode valuesNode = objectMapper.readTree(jsonData)
                        .get("data")
                        .get(coin.getName())
                        .get("transactions");

                for (JsonNode node : valuesNode) {
                    float[] values = new float[coinFundCols.length];
                    for (int i = 0; i < values.length; i++) {
                        values[i] = (float) node.get(coinFundCols[i]).asDouble();
                    }

                    Date timestamp;
                    switch (coin) {
                        case BTCUSDT -> {
                            if (interval == TimeFrame.ONE_HOUR || interval == TimeFrame.FOUR_HOUR) {
                                int year = node.get("block").get("timestamp").get("year").asInt();
                                int month = node.get("block").get("timestamp").get("month").asInt();
                                int day = node.get("block").get("timestamp").get("dayOfMonth").asInt();
                                int hour = node.get("block").get("timestamp").get("hour").asInt();
                                timestamp = sdfFullISO.parse(String.format("%d-%d-%d %d:00:00", year, month, day, hour));
                            } else {
                                timestamp = sdfFullISO.parse(node.get("block").get("timestamp").get("time").asText());
                            }
                        }
                        case ETHUSDT -> {
                            int year = node.get("block").get("timestamp").get("year").asInt();
                            int month = node.get("block").get("timestamp").get("month").asInt();
                            int day = node.get("block").get("timestamp").get("dayOfMonth").asInt();
                            int hour = node.get("block").get("timestamp").get("hour").asInt();
                            if (interval == TimeFrame.ONE_HOUR || interval == TimeFrame.FOUR_HOUR) {
                                timestamp = sdfFullISO.parse(String.format("%d-%d-%d %d:00:00", year, month, day, hour));
                            } else {
                                int minute = node.get("block").get("timestamp").get("minute").asInt();
                                timestamp = sdfFullISO.parse(String.format("%d-%d-%d %d:%d:00", year, month, day, hour, minute));
                            }
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + coin);
                    };

                    temp.put(timestamp, values);
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
        calendar.setTimeZone(TimeZone.getTimeZone(defaultZone));
        calendar.setTime(date);
        int minuteOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        calendar.add(Calendar.MINUTE, -(minuteOfDay % intervalInMinutes));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private TreeMap<Date, float[]> mergeData(TreeMap<Date, float[]> rawData, int interval) {
        TreeMap<Date, float[]> mergedData = new TreeMap<>();

        Date currentIntervalStart = null;
        float[] sumValues = new float[rawData.firstEntry().getValue().length];
        int count = 0;

        for (Map.Entry<Date, float[]> entry : rawData.entrySet()) {
            Date timestamp = entry.getKey();
            float[] values = entry.getValue();

            Date roundedDate = roundDown(timestamp, interval);

            if (currentIntervalStart != null && !currentIntervalStart.equals(roundedDate)) {
                sumValues[sumValues.length-1] /= count;
                mergedData.put(currentIntervalStart, sumValues);

                sumValues = new float[values.length];
                count = 0;
            }

            for (int i = 0; i < values.length; i++) {
                sumValues[i] += values[i];
            }
            count++;

            currentIntervalStart = roundedDate;
        }

        if (currentIntervalStart != null) {
            sumValues[sumValues.length-1] /= count;
            mergedData.put(currentIntervalStart, sumValues);
        }

        return mergedData;
    }

}
