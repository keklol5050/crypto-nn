package com.crypto.analysis.main.core.updater;

import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.select.fundamental.FundamentalStock;
import com.crypto.analysis.main.core.data_utils.utils.BinanceDataUtil;
import com.crypto.analysis.main.core.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.core.vo.indication.BTCDOMObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;

import static com.crypto.analysis.main.core.data_utils.select.StaticData.*;

public class FundamentalUpdater {
    private final FundamentalDataUtil fdUtil;
    private List<String> lines;
    private ArrayList<Date> dates;

    public FundamentalUpdater(FundamentalDataUtil fdUtil) {
        this.fdUtil = fdUtil;
    }

    public void update(FundamentalStock stock) {
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

    private void update(Path path, TreeMap<Date, Double> data) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(String.valueOf(path), true))) {

            lines = Files.readAllLines(path);
            lines.removeFirst();

            dates = new ArrayList<Date>();

            for (String line : lines) {
                dates.add(sdfFullISO.parse(line.split(";")[0]));
            }

            if (!dates.contains(data.firstKey())) throw new IllegalStateException("Data list is not full");

            for (Map.Entry<Date, Double> entry : data.entrySet()) {
                if (entry.getKey().after(dates.getLast()) && !dates.contains(entry.getKey()))
                    writer.print(String.format("\n%s;%s", sdfFullISO.format(entry.getKey()), entry.getValue()));
            }
            writer.close();
            System.out.printf("Updated stock data %s%n", path);
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
                dates.add(new Date(Long.parseLong(line.split(",")[0])));
            }

            if (!dates.contains(BTCDom.getMap().firstKey())) throw new IllegalStateException("Data list is not full");

            for (Map.Entry<Date, Double> entry : BTCDom.getMap().entrySet()) {
                if (entry.getKey().after(dates.getLast()) && !dates.contains(entry.getKey()))
                    writer.print(String.format("\n%s,%s", entry.getKey().getTime(), entry.getValue()));
            }

            System.out.println("BTCDOM data updated at path " + pathToBTCDOM);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
