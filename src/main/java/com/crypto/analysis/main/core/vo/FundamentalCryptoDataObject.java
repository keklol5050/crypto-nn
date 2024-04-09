package com.crypto.analysis.main.core.vo;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;

import java.util.Arrays;
import java.util.Date;


public class FundamentalCryptoDataObject {
    private final Coin coin;
    private final Date createTime;

    private final double[] data; // count, fee_value, fee_average, input_count, input_value, mined_value, output_count, output_value

    public FundamentalCryptoDataObject(Coin coin, double[] data) {
        this.coin = coin;
        this.createTime = new Date();

        this.data = data;
    }

    @Override
    public String toString() {
        return "FundamentalCryptoDataObject{" +
                "coin=" + coin +
                ", createTime=" + createTime +
                ", data=" + Arrays.toString(data) +
                '}';
    }

    public double[] getParamArray() {
        return data;
    }
}
