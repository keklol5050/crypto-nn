package com.crypto.analysis.main;

import com.crypto.analysis.main.core.model.ModelLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import java.util.Arrays;

public class ModeLoadTest {
    public static void main(String[] args) {
        MultiLayerNetwork model = ModelLoader.loadNetwork("D:\\model15.zip");
        assert model != null;
        System.out.println(model.summary());
        System.out.println(model.score());
        System.out.println(model.getUpdater());
        System.out.println(model.getListeners());
        System.out.println(Arrays.toString(model.getLayers()));
        System.out.println(model.calcRegularizationScore(true));
        System.out.println(model.getDefaultConfiguration());
    }
}
