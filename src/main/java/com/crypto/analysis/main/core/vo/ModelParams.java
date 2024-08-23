package com.crypto.analysis.main.core.vo;

import ai.djl.Device;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import com.crypto.analysis.main.core.data_utils.select.coin.Coin;
import com.crypto.analysis.main.core.data_utils.select.coin.DataLength;
import com.crypto.analysis.main.core.data_utils.select.coin.TimeFrame;
import com.crypto.analysis.main.core.data_utils.utils.PropertiesUtil;
import lombok.Getter;

import java.nio.file.Path;
import java.util.Arrays;

@Getter
public class ModelParams {
    public static final int DEFAULT_NUM_FEATURES = 61;
    public static final int DEFAULT_BATCH_SIZE = PropertiesUtil.getPropertyAsInteger("model.batch_size");
    public static final Device DEFAULT_DEVICE = PropertiesUtil.getProperty("djl.device").equals("gpu") ? Device.gpu() : Device.cpu();
    public static final DataType DEFAULT_DATA_TYPE = DataType.valueOf(PropertiesUtil.getProperty("djl.data_type"));
    public static final NDManager manager = NDManager.newBaseManager(DEFAULT_DEVICE);

    private Coin coin;
    private int numFeatures;
    private Path folderPath;
    private TimeFrame timeFrame;
    private DataLength dataLength;
    private int batchSize;
    private int convLayers;
    private int[] convFilters;
    private int convKernelSize;
    private int convPoolKernelSize;
    private int recurrentLayers;
    private int recurrentStateSize;
    private float recurrentDropRate;
//    private float dropRate;
    private Device device;
    private DataType dataType;
    private String modelName;
    private ModelParams self() {
        return this;
    }

    public ModelParams setCoin(Coin coin) {
        this.coin = coin;
        return self();
    }

    public ModelParams setNumFeatures(int numFeatures) {
        this.numFeatures = numFeatures;
        return self();
    }

    public ModelParams setFolderPath(Path folderPath) {
        this.folderPath = folderPath;
        return self();
    }

    public ModelParams setTimeFrame(TimeFrame timeFrame) {
        this.timeFrame = timeFrame;
        return self();
    }

    public ModelParams setDataLength(DataLength dl) {
        this.dataLength = dl;
        return self();
    }

    public ModelParams setBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return self();
    }

    public ModelParams setConvLayers(int count) {
        this.convLayers = count;
        return self();
    }

    public ModelParams setConvFilters(int... count) {
        this.convFilters = count;
        return self();
    }

    public ModelParams setConvKernelSize(int size) {
        this.convKernelSize = size;
        return self();
    }

    public ModelParams setConvPoolKernelSize(int size) {
        this.convPoolKernelSize = size;
        return self();
    }

    public ModelParams setRecurrentLayers(int count) {
        this.recurrentLayers = count;
        return self();
    }

    public ModelParams setRecurrentStateSize(int size) {
        this.recurrentStateSize = size;
        return self();
    }

    public ModelParams setRecurrentDropRate(float recurrentDropRate) {
        this.recurrentDropRate = recurrentDropRate;
        return self();
    }

//    public ModelParams setDropRate(float dropRate) {
//        this.dropRate = dropRate;
//        return self();
//    }

    public ModelParams setDevice(Device device) {
        this.device = device;
        return self();
    }
    
    public ModelParams setDataType(DataType dataType) {
        this.dataType = dataType;
        return self();
    }

    public ModelParams setModelName(String modelName) {
        this.modelName = modelName;
        return self();
    }

    public static ModelParams getDefault(Coin coin, TimeFrame tf, DataLength dl, Path folderPath) {
        int convLayers = 3;

        int[] convFilters = switch (dl) {
            case S100_5, L120_6 -> new int[] {128, 256,  384};
            case X180_9, XL240_12 -> new int[] {160, 280, 420};
            default -> throw new IllegalStateException("Unexpected value: " + dl);
        };

        int kernelSize = switch (dl) {
            case S100_5 -> 5;
            case L120_6 -> 7;
            case X180_9 -> 9;
            case XL240_12 -> 10;
            default -> throw new IllegalStateException("Unexpected value: " + dl);
        };

        int poolKernelSize = switch (dl) {
            case S100_5, L120_6  -> 2;
            case X180_9, XL240_12 -> 3;
            default -> throw new IllegalStateException("Unexpected value: " + dl);
        };

        int recurrentLayers = switch (dl) {
            case S100_5, L120_6, X180_9 -> 2;
            case XL240_12 -> 3;
            default -> throw new IllegalStateException("Unexpected value: " + dl);
        };

        int recurrentStateSize = switch (dl) {
            case S100_5, L120_6 -> 256;
            case X180_9 -> 384;
            case XL240_12 -> 300;
            default -> throw new IllegalStateException("Unexpected value: " + dl);
        };

        float recurrentDropRate = switch (dl) {
            case S100_5 -> 0.01f;
            case L120_6, X180_9 -> 0.025f;
            case XL240_12 -> 0.05f;
            default -> throw new IllegalStateException("Unexpected value: " + dl);
        };

//        float dropRate = switch (dl) {
//            case S100_5 -> switch (tf) {
//                case FIFTEEN_MINUTES -> 0.15F;
//                case FOUR_HOUR -> 0.30F;
//                case ONE_HOUR -> 0.20F;
//            };
//            case L120_6 -> switch (tf) {
//                case FIFTEEN_MINUTES -> 0.20F;
//                case FOUR_HOUR -> 0.35F;
//                case ONE_HOUR -> 0.25F;
//            };
//            case X180_9 -> switch (tf) {
//                case FIFTEEN_MINUTES -> 0.250F;
//                case FOUR_HOUR -> 0.40F;
//                case ONE_HOUR -> 0.30F;
//            };
//            case XL240_12 ->switch (tf) {
//                case FIFTEEN_MINUTES -> 0.30F;
//                case FOUR_HOUR -> 0.45F;
//                case ONE_HOUR -> 0.35F;
//            };
//        };

        return new ModelParams()
                .setCoin(coin)
                .setNumFeatures(DEFAULT_NUM_FEATURES)
                .setFolderPath(folderPath)
                .setTimeFrame(tf)
                .setDataLength(dl)
                .setBatchSize(DEFAULT_BATCH_SIZE)
                .setConvLayers(convLayers)
                .setConvFilters(convFilters)
                .setConvKernelSize(kernelSize)
                .setConvPoolKernelSize(poolKernelSize)
                .setRecurrentLayers(recurrentLayers)
                .setRecurrentStateSize(recurrentStateSize)
                .setRecurrentDropRate(recurrentDropRate)
//                .setDropRate(dropRate)
                .setDevice(DEFAULT_DEVICE)
                .setDataType(DEFAULT_DATA_TYPE)
                .setModelName(String.format("Model-%s-%s-%s", coin, tf, dl));
    }

    @Override
    public String toString() {
        return "\nModelParams{" +
                "coin=" + coin +
                ", numFeatures=" + numFeatures +
                ", folderPath=" + folderPath +
                ", timeFrame=" + timeFrame +
                ",\n dataLength=" + dataLength +
                ", batchSize=" + batchSize +
                ", convFilters=" + Arrays.toString(convFilters) +
                ", convKernelSize=" + convKernelSize +
                ",\n convPoolKernelSize=" + convPoolKernelSize +
                ", recurrentLayers=" + recurrentLayers +
                ", recurrentStateSize=" + recurrentStateSize +
                ", \n recurrentDropRate=" + recurrentDropRate +
//                ", dropRate=" + dropRate +
                ", device=" + device +
                ", dataType=" + dataType +
                ", modelName='" + modelName + '\'' +
                '}';
    }
}
