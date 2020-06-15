package ch.so.agi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.interlis.iox.IoxException;

public class Gpkg2Csv {
    Logger log = LoggerFactory.getLogger(Gpkg2Dxf.class);

    public void convert(String fileName) throws IOException, IoxException {
        File tmpFolder = Files.createTempDirectory("gpkgws-").toFile();
        if (!tmpFolder.exists()) {
            tmpFolder.mkdirs();
        }
        log.info("tmpFolder {}", tmpFolder.getAbsolutePath());
        
        
        // Geometrie-Konversion hier machen und zu WKT (ist dann String für CSVWriter) konvertieren.
        // Wie komme ich an die Geometrie-Spalte? -> Siehe DXF.

    }
}
