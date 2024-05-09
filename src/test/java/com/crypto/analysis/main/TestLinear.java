package com.crypto.analysis.main;

import com.crypto.analysis.main.core.data_utils.utils.mutils.ModelLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

public class TestLinear {
    public static void main(String[] args) {
        MultiLayerNetwork model = ModelLoader.loadNetwork("D:\\model9.zip");
        model.init();

        System.out.println(model.summary());

    }

}
