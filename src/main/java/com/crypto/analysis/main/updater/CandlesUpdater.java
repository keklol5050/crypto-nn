package com.crypto.analysis.main.updater;

import com.crypto.analysis.main.data_utils.select.coin.Coin;
import com.crypto.analysis.main.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.data_utils.utils.binance.BinanceDataUtil;
import com.crypto.analysis.main.vo.CandleObject;
import com.crypto.analysis.main.vo.indication.FundingHistoryObject;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.crypto.analysis.main.data_utils.select.StaticData.*;


public class CandlesUpdater {
    private Coin coin;

    private List<String> lines;
    private LinkedList<Date> dates;

    public CandlesUpdater(Coin coin) {
        this.coin = coin;
    }

    public void update(TimeFrame tf) {
        Path path = switch (tf) {
            case FIFTEEN_MINUTES -> pathToBTCCandles15m;
            case ONE_HOUR -> pathToBTCCandles1h;
            case FOUR_HOUR -> pathToBTCCandles4h;
            default -> throw new RuntimeException("Invalid time frame");
        };

        try (PrintWriter writer = new PrintWriter(new FileWriter(String.valueOf(path), true))) {

            lines = Files.readAllLines(path);
            if (lines.get(0).matches("[a-zA-Z]")) lines.remove(0);

            dates = new LinkedList<>();

            for (String line : lines) {
                dates.add(new Date(Long.parseLong(line.split(",")[0])));
            }

            LinkedList<CandleObject> candles = BinanceDataUtil.getCandles(coin, tf,  1500);
            candles.removeLast();

            if (!dates.contains(candles.getFirst().getOpenTime())) throw new IllegalStateException("Data list is not full");

            for (CandleObject candle : candles) {
                if (!dates.contains(candle.getOpenTime())) writer.println(String.format("%s,%s,%s,%s,%s,%s,%s",
                        candle.getOpenTime().getTime(), candle.getOpen(), candle.getHigh(), candle.getLow(),
                        candle.getClose(), candle.getVolume(), candle.getCloseTime().getTime()));
            }
            System.out.printf("Candles data updated at path %s%n", path.getFileName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
