package com.crypto.analysis.main.updater;

import com.crypto.analysis.main.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.data_utils.select.fundamental.FundamentalStock;
import com.crypto.analysis.main.data_utils.utils.binance.BinanceDataUtil;
import com.crypto.analysis.main.fundamental.stock.FundamentalDataUtil;
import com.crypto.analysis.main.vo.indication.BTCDOMObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;

import static com.crypto.analysis.main.data_utils.select.StaticData.*;

public class FundamentalUpdater {
    private final FundamentalDataUtil fdUtil;

    private PrintWriter writer;
    private List<String> lines;
    private HashSet<Date> dates;
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
       };
    }

    private void update(Path path, TreeMap<Date, Double> data) {
        try {
            writer = new PrintWriter(new FileWriter(String.valueOf(path), true));

            lines = Files.readAllLines(path);
            if (lines.get(0).matches("[a-zA-Z]")) lines.remove(0);

            dates = new HashSet<Date>();

            for (String line : lines) {
                dates.add(sdfFullISO.parse(line.split(",")[0]));
            }

            if (!dates.contains(data.firstKey())) throw new IllegalStateException("Data list is not full");

            for (Map.Entry<Date, Double> entry : data.entrySet()) {
                if (!dates.contains(entry.getKey())) writer.println(String.format("%s;%s", sdfFullISO.format(entry.getKey()), entry.getValue()));
            }
            writer.close();
            System.out.printf("Updated %s%n", path.getFileName());
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateBTCDOM() {
        BTCDOMObject BTCDom = BinanceDataUtil.getBTCDomination(TimeFrame.FIFTEEN_MINUTES);
        try (PrintWriter writer = new PrintWriter(new FileWriter(String.valueOf(pathToBTCDOM), true))) {

            lines = Files.readAllLines(pathToBTCDOM);
            if (lines.get(0).matches("[a-zA-Z]")) lines.remove(0);

            dates = new LinkedHashSet<Date>();

            for (String line : lines) {
                dates.add(new Date(Long.parseLong(line.split(",")[0])));
            }

            if (!dates.contains(BTCDom.getMap().firstKey())) throw new IllegalStateException("Data list is not full");

            for (Map.Entry<Date, Double> entry : BTCDom.getMap().entrySet()) {
                if (!dates.contains(entry.getKey()))
                    writer.println(String.format("%s,%s", entry.getKey().getTime(), entry.getValue()));
            }

            System.out.println("BTCDOM data updated");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
