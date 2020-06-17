package ch.so.agi;

import java.io.IOException;

import ch.interlis.iox.IoxException;

public class App {
    public static void main(String[] args) throws Exception {
        //Gpkg3Shp gpkg3shp = new Gpkg3Shp();
        //gpkg3shp.convert("./src/test/data/ch.so.agi.av-gb-administrative-einteilung.gpkg");
        
        Gpkg2Csv gpkg2cvs = new Gpkg2Csv();
        gpkg2cvs.convert("./src/test/data/ch.so.agi.av-gb-administrative-einteilung.gpkg");
        

        System.out.println("Hallo Welt.");
    }
}
