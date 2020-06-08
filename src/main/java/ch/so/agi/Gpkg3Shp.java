package ch.so.agi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ehi.basics.settings.Settings;
import ch.interlis.iox.IoxEvent;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.ObjectEvent;
import ch.interlis.iox_j.EndBasketEvent;
import ch.interlis.iox_j.EndTransferEvent;
import ch.interlis.ioxwkf.gpkg.GeoPackageReader;
import ch.interlis.ioxwkf.shp.ShapeWriter;

public class Gpkg3Shp {
    Logger log = LoggerFactory.getLogger(Gpkg3Shp.class);

    public Gpkg3Shp() {}
    
    public void convert(String fileName, String tableName) throws IOException, IoxException {
        File tmpFolder = Files.createTempDirectory("gpkgws-").toFile();
        if (!tmpFolder.exists()) {
            tmpFolder.mkdirs();
        }
        log.info("tmpFolder {}", tmpFolder.getAbsolutePath());

        Settings settings = new Settings();
        ShapeWriter writer = new ShapeWriter(new File("/Users/stefan/tmp/"+tableName+".shp"), settings);
        writer.setDefaultSridCode("2056");
        
        GeoPackageReader reader = new GeoPackageReader(new File(fileName), tableName);        
        IoxEvent event = reader.read();
        while (event instanceof IoxEvent) {
            if (event instanceof ObjectEvent) {
                writer.write(event);
            }
            event = reader.read();
        }
        
        writer.write(new EndBasketEvent());
        writer.write(new EndTransferEvent());

        if (writer != null) {
            writer.close();
            writer = null;
        }
        if (reader != null) {
            reader.close();
            reader = null;
        }
    }
}
