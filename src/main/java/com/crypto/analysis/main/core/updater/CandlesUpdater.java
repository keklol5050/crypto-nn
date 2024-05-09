package com.crypto.analysis.main.core.updater;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.BinanceDataUtil;
import com.crypto.analysis.main.core.vo.CandleObject;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import static com.crypto.analysis.main.core.data_utils.select.StaticData.*;


public class CandlesUpdater {
    private Coin coin;

    private List<String> lines;
    private ArrayList<Date> dates;

    public CandlesUpdater(Coin coin) {
        this.coin = coin;
    }

    public void update(TimeFrame tf) {
        Path path = switch (coin) {
            case BTCUSDT -> switch (tf) {
                case FIFTEEN_MINUTES -> pathToBTCCandles15m;
                case ONE_HOUR -> pathToBTCCandles1h;
                case FOUR_HOUR -> pathToBTCCandles4h;
                default -> throw new RuntimeException("Invalid time frame");
            };
            default -> throw new IllegalStateException("Unexpected value: " + coin);
        };

        try (PrintWriter writer = new PrintWriter(new FileWriter(String.valueOf(path), true))) {

            lines = Files.readAllLines(path);
            lines.removeFirst();

            dates = new ArrayList<>();

            for (String line : lines) {
                dates.add(new Date(Long.parseLong(line.split(",")[0])));
            }

            ArrayList<CandleObject> candles = BinanceDataUtil.getCandles(coin, tf, 1500);
            candles.removeLast();
            candles.removeLast();

            if (!dates.contains(candles.getFirst().getOpenTime()))
                throw new IllegalStateException("Data list is not full");

            for (CandleObject candle : candles) {
                if (candle.getOpenTime().after(dates.getLast()) && !dates.contains(candle.getOpenTime()))
                    writer.print(String.format("\n%s,%s,%s,%s,%s,%s,%s",
                            candle.getOpenTime().getTime(), candle.getOpen(), candle.getHigh(), candle.getLow(),
                            candle.getClose(), candle.getVolume(), candle.getCloseTime().getTime()));
            }
            System.out.printf("Candles data " + coin + " updated at path %s%n", path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
