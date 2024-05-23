package com.crypto.analysis.main.core.vo;

import com.crypto.analysis.main.core.data_utils.select.coin.Coin;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;


public class FundamentalCryptoDataObject {
    private final Coin coin;
    private final Date createTime;

    private final float[] data; // count, fee_value, fee_average, input_count, input_value, mined_value, output_count, output_value

    public FundamentalCryptoDataObject(Coin coin, float[] data) {
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

    public float[] getParamArray() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FundamentalCryptoDataObject that)) return false;
        return coin == that.coin && Objects.equals(createTime, that.createTime) && Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(coin, createTime);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }
}
