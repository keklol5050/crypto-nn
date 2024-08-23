package com.crypto.analysis.main.core.updater;

import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.select.fundamental.FundamentalStock;
import com.crypto.analysis.main.core.data_utils.utils.BinanceDataUtil;
import com.crypto.analysis.main.core.data_utils.utils.PropertiesUtil;
import com.crypto.analysis.main.core.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.core.vo.indication.BTCDOMObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;

import static com.crypto.analysis.main.core.data_utils.select.StaticUtils.sdfFullISO;

public class FundamentalUpdater {
    public static final Path pathToDJI = Path.of(new File(FundamentalUpdater.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParent() + PropertiesUtil.getProperty("data.data_path") + "DJI.csv");
    public static final Path pathToDXY = Path.of(new File(FundamentalUpdater.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParent() + PropertiesUtil.getProperty("data.data_path") + "DXY.csv");
    public static final Path pathToNDX = Path.of(new File(FundamentalUpdater.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParent() + PropertiesUtil.getProperty("data.data_path") + "NDX.csv");
    public static final Path pathToSPX = Path.of(new File(FundamentalUpdater.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParent() + PropertiesUtil.getProperty("data.data_path") + "SPX.csv");
    public static final Path pathToVIX = Path.of(new File(FundamentalUpdater.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParent() + PropertiesUtil.getProperty("data.data_path") + "VIX.csv");
    public static final Path pathToGOLD = Path.of(new File(FundamentalUpdater.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParent() + PropertiesUtil.getProperty("data.data_path") + "XAUUSD.csv");
    public static final Path pathToBTCDOM = Path.of(new File(FundamentalUpdater.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParent() + PropertiesUtil.getProperty("data.data_path") + "BTCDOM.csv");

    private final FundamentalDataUtil fdUtil;
    private List<String> lines;
    private ArrayList<Date> dates;

    private static final Logger logger = LoggerFactory.getLogger(FundamentalUpdater.class);

    public FundamentalUpdater(FundamentalDataUtil fdUtil) {
        this.fdUtil = fdUtil;
        logger.info("Initializing fundamental data updater");
    }

    public void update(FundamentalStock stock) {
        fdUtil.init();
        switch (stock) {
            case SPX -> update(pathToSPX, fdUtil.getSPX());
            case DXY -> update(pathToDXY, fdUtil.getDXY());
            case DJI -> update(pathToDJI, fdUtil.getDJI());
            case VIX -> update(pathToVIX, fdUtil.getVIX());
            case NDX -> update(pathToNDX, fdUtil.getNDX());
            case GOLD -> update(pathToGOLD, fdUtil.getGOLD());
        }
        ;
    }

    private void update(Path path, TreeMap<Date, float[]> data) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(String.valueOf(path), true))) {

            lines = Files.readAllLines(path);
            lines.removeFirst();

            dates = new ArrayList<Date>();

            for (String line : lines) {
                dates.add(sdfFullISO.parse(line.split(",")[0]));
            }

            if (!dates.contains(data.firstKey())) throw new IllegalStateException("Data list is not full");

            for (Map.Entry<Date, float[]> entry : data.entrySet()) {
                if (entry.getKey().after(dates.getLast()) && !dates.contains(entry.getKey())) {
                    writer.print('\n' + sdfFullISO.format(entry.getKey()) + ',' + Arrays.toString(entry.getValue()).replaceAll("[\\[\\] ]", ""));
                }
            }
            writer.close();
           logger.info("Updated stock data {}", path);
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateBTCDOM() {
        BTCDOMObject BTCDom = BinanceDataUtil.getBTCDomination(TimeFrame.FIFTEEN_MINUTES);
        try (PrintWriter writer = new PrintWriter(new FileWriter(String.valueOf(pathToBTCDOM), true))) {

            lines = Files.readAllLines(pathToBTCDOM);
            lines.removeFirst();

            dates = new ArrayList<Date>();

            for (String line : lines) {
                dates.add(sdfFullISO.parse(line.split(",")[0]));
            }

            if (!dates.contains(BTCDom.getMap().firstKey())) throw new IllegalStateException("Data list is not full");

            for (Map.Entry<Date, float[]> entry : BTCDom.getMap().entrySet()) {
                if (entry.getKey().after(dates.getLast()) && !dates.contains(entry.getKey())) {
                    writer.print('\n' + sdfFullISO.format(entry.getKey()) + ',' + Arrays.toString(entry.getValue()).replaceAll("[\\[\\] ]", ""));
                }
            }

            logger.info("BTCDOM data updated at path {}", pathToBTCDOM);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
