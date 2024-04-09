package com.crypto.analysis.main.core.model;

import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;

import java.io.File;
import java.io.IOException;

public class ModelLoader {
    public static void saveModel(Model model, String filePath) {
        try {
            ModelSerializer.writeModel(model, new File(filePath), true);
            System.out.println("Model saved on the path: " + filePath);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    public static ComputationGraph loadGraph(String filePath) {
        try {
            return ModelSerializer.restoreComputationGraph(new File(filePath), true);
        } catch (IOException e) {
            e.printStackTrace(System.out);
            return null;
        }
    }

    public static MultiLayerNetwork loadNetwork(String filePath) {
        try {
            return ModelSerializer.restoreMultiLayerNetwork(new File(filePath), true);
        } catch (IOException e) {
            e.printStackTrace(System.out);
            return null;
        }
    }
}