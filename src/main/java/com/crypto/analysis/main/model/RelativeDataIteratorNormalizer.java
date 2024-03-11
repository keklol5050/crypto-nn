package com.crypto.analysis.main.model;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;

import java.io.*;

public class RelativeDataIteratorNormalizer implements Serializable {
    private final DataSetIterator[] iterators;
    private final NormalizerMinMaxScaler[] normalizers;
    public RelativeDataIteratorNormalizer(DataSetIterator... iterators) {
        if (iterators==null || iterators.length==0) throw new NullPointerException();
        this.iterators = iterators;
        this.normalizers = new NormalizerMinMaxScaler[iterators.length];
        init();
    }

    private void init() {
        for (int i = 0; i < iterators.length; i++) {
            DataSetIterator iterator = iterators[i];

            NormalizerMinMaxScaler normalizer = new NormalizerMinMaxScaler();
            normalizer.fitLabel(true);

            normalizer.fit(iterator);
            iterator.reset();
            iterator.setPreProcessor(normalizer);

            normalizers[i] = normalizer;
        }
    }

    public void transform(INDArray input) {
        if (input==null || input.isEmpty()) throw new NullPointerException();
        for (int i = 0; i < iterators.length; i++) {
            iterators[i].reset();
            while (iterators[i].hasNext()) {
                DataSet set = iterators[i].next();
                long a = set.getFeatures().size(1);
                long b = input.size(1);
                if (a==b) {
                   normalizers[i].transform(input);
                   return;
                }
            }
        }
        throw new IllegalArgumentException();
    }

    public void revertResult(INDArray result) {
        if (result==null || result.isEmpty()) throw new NullPointerException();
        for (int i = 0; i < iterators.length; i++) {
            iterators[i].reset();
            while (iterators[i].hasNext()) {
                DataSet set = iterators[i].next();
                long a = set.getLabels().size(1);
                long b = result.size(1);
                if (a==b) {
                    normalizers[i].revertLabels(result);
                    return;
                }
            }
        }
        throw new IllegalArgumentException();
    }

    public void saveNormalizer(String basePath) throws Exception {
        if (basePath==null || basePath.isEmpty()) throw new NullPointerException();
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(basePath));
        out.writeObject(this);
        System.out.println("Normalizer saved on the path: " + basePath);
    }

    public static RelativeDataIteratorNormalizer loadNormalizer(String basePath) throws Exception {
        if (basePath==null || basePath.isEmpty()) throw new NullPointerException();
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(basePath));
        return (RelativeDataIteratorNormalizer) in.readObject();
    }
}
