package ch.so.agi;

import java.io.IOException;

import ch.interlis.iox.IoxException;

public class App {
    public static void main(String[] args) throws IOException, IoxException {

        Gpkg3Shp gpkg3shp = new Gpkg3Shp();
        gpkg3shp.convert("./src/test/data/2581.gpkg", "nutzungsplanung_grundnutzung");
        
        
        
        System.out.println("Hallo Welt.");
    }
}
