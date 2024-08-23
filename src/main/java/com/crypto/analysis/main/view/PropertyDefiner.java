package com.crypto.analysis.main.view;

import ch.qos.logback.core.PropertyDefinerBase;

import java.io.File;

public class PropertyDefiner extends PropertyDefinerBase {
    @Override
    public String getPropertyValue() {
        return new File(PropertyDefiner.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParent();
    }

    public static void main(String[] args) {
        System.out.println(new PropertyDefiner().getPropertyValue());
        System.out.println();
    }
}