package com.crypto.analysis.main.data_utils.nn;

import java.awt.*;
import java.io.Console;
import java.io.IOException;
public class Main{
    public static void main (String [] args) throws IOException{
        Console console = System.console();
        if(console == null && !GraphicsEnvironment.isHeadless()){
            String filename = Main.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
            Runtime.getRuntime().exec(new String[]{"cmd","/c","start","cmd","/k","java -jar \"" + filename + "\""});
        }else{
            Solution.main(new String[0]);
            System.out.println("Type 'exit' to close the console");
        }
    }
}
