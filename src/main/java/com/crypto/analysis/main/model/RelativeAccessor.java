package com.crypto.analysis.main.model;

import com.crypto.analysis.main.data_utils.normalizers.BatchNormalizer;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.*;
import java.util.HashMap;

public class RelativeAccessor implements Serializable {
    private final HashMap<Integer, INDArray> masks;
    private final HashMap<Integer, BatchNormalizer> normalizers;
    public RelativeAccessor(HashMap<Integer, INDArray> masks,
                            HashMap<Integer, BatchNormalizer> normalizers) {
        this.masks = masks;
        this.normalizers = normalizers;
    }

    public INDArray getMask(int sequenceLength) {
        return masks.get(sequenceLength);
    }

    public BatchNormalizer getNormalizer(int sequenceLength) {
        return normalizers.get(sequenceLength);
    }

    public void normalize(double[][] input, boolean fit) {
        BatchNormalizer normalizer = normalizers.get(input[0].length);
        if (fit) normalizer.fitHorizontal(input);
        normalizer.transformHorizontal(input);
    }

    public void revertFeatures(double[][] input) {
        BatchNormalizer normalizer = normalizers.get(input[0].length);
        normalizer.revertFeaturesVertical(input);
    }

    public void revertLabels(double[][] key, double[][] input) {
        BatchNormalizer normalizer = normalizers.get(key[0].length);
        normalizer.revertLabelsVertical(key, input);
    }

    public void saveAccessor(String basePath) {
        if (basePath == null || basePath.isEmpty()) throw new NullPointerException();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(basePath));
            out.writeObject(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Accessor saved on the path: " + basePath);
    }

    public static RelativeAccessor loadAccessor(String basePath) {
        if (basePath == null || basePath.isEmpty()) throw new NullPointerException();
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(basePath));
            return (RelativeAccessor) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
