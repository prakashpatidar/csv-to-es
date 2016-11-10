package com.ubs.ii;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by praka on 11/6/2016.
 */
public class Driver {
    public static void main(String[] args) {
    /*    Notifier notifier = new Notifier();
        try {
            notifier.watch("data");
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        String currentPath = new File("").getAbsolutePath();
        Ingester ingester = new Ingester();
        List<String> files = new ArrayList<>();
        files.add(currentPath + File.separator +  "data"+ File.separator+ "Holding.holding");
        ingester.ingest(files);
    }
}
