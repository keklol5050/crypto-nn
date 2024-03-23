package com.crypto.analysis.main;

import com.crypto.analysis.main.data_utils.select.StaticData;
import com.crypto.analysis.main.data_utils.select.coin.Coin;
import com.crypto.analysis.main.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.fundamental.crypto.BitQueryUtil;
import org.tensorflow.distruntime.TracingRequest;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class SetCreator {
    public static void main(String[] args) throws IOException, ParseException {
        BitQueryUtil util = new BitQueryUtil(Coin.BTCUSDT, TimeFrame.FOUR_HOUR);

        PrintWriter writer = new PrintWriter(new FileWriter("C:\\Users\\keklo\\OneDrive\\Рабочий стол\\Новая папка\\ll\\fund_crypto_4h.csv", true));
        writer.println("open_time,count,fee_value,fee_average,input_count,input_value,mined_value,output_count,output_value");

        LinkedHashMap<String, String> datas = new LinkedHashMap<String, String>();
        datas.put("2022-06-01", "2022-08-01");
        datas.put("2022-08-01", "2022-10-01");
        datas.put("2022-10-01", "2022-12-01");
        datas.put("2022-12-01", "2023-02-01");
        datas.put("2023-02-01", "2023-04-01");
        datas.put("2023-04-01", "2023-06-01");
        datas.put("2023-06-01", "2023-08-01");
        datas.put("2023-08-01", "2023-10-01");
        datas.put("2023-10-01", "2023-12-01");
        datas.put("2023-12-01", "2024-02-01");
        datas.put("2024-02-01", "2024-03-01");

       for (Map.Entry<String,String> entryD : datas.entrySet()) {
           util.initData(StaticData.sdfShortISO.parse(entryD.getKey()), StaticData.sdfShortISO.parse(entryD.getValue()));
           TreeMap<Date, double[]> data =util.getData();
           for (Map.Entry<Date, double[]> entry : data.entrySet()) {
               writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s", StaticData.sdfFullISO.format(entry.getKey()), entry.getValue()[0], entry.getValue()[1], entry.getValue()[2], entry.getValue()[3], entry.getValue()[4], entry.getValue()[5], entry.getValue()[6], entry.getValue()[7]));
               writer.flush();
           }
       }
    }
}
