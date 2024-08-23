package com.crypto.analysis.main.core.updater;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.BinanceDataUtil;
import com.crypto.analysis.main.core.data_utils.utils.PropertiesUtil;
import com.crypto.analysis.main.core.vo.CandleObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.crypto.analysis.main.core.data_utils.select.StaticUtils.sdfFullISO;


public class CandlesUpdater {
    private Coin coin;

    private List<String> lines;
    private ArrayList<Date> dates;

    private static final Logger logger = LoggerFactory.getLogger(CandlesUpdater.class);

    public CandlesUpdater(Coin coin) {
        this.coin = coin;
        logger.info("Initializing updater with coin {}", coin);
    }

    public void update(TimeFrame tf) {
        Path path = Path.of(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getParent() + PropertiesUtil.getProperty("data.data_path") + coin + "/candles/" + tf.getTimeFrame() + ".csv");
        logger.info("Updating time frame {} for coin {}", tf, coin);

        try (PrintWriter writer = new PrintWriter(new FileWriter(String.valueOf(path), true))) {
            lines = Files.readAllLines(path);
            lines.removeFirst();

            dates = new ArrayList<>();

            for (String line : lines) {
                dates.add(sdfFullISO.parse(line.split(",")[0]));
            }

            ArrayList<CandleObject> candles = BinanceDataUtil.getCandles(coin, tf, 1500);
            candles.removeLast();
            candles.removeLast();

            if (!dates.contains(candles.getFirst().getOpenTime()))
                throw new IllegalStateException("Data list is not full");

            for (CandleObject candle : candles) {
                if (candle.getOpenTime().after(dates.getLast()) && !dates.contains(candle.getOpenTime()))
                    writer.print(String.format("\n%s,%s,%s,%s,%s,%s,%s",
                            sdfFullISO.format(candle.getOpenTime()), candle.getOpen(), candle.getHigh(), candle.getLow(),
                            candle.getClose(), candle.getVolume(), sdfFullISO.format(candle.getCloseTime())));
            }
            logger.info("Candles data {} updated at path {}", coin, path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
