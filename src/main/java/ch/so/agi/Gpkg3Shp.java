package ch.so.agi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

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
    
    public void convert(String fileName) throws IOException, IoxException {
        File tmpFolder = Files.createTempDirectory("gpkgws-").toFile();
        if (!tmpFolder.exists()) {
            tmpFolder.mkdirs();
        }
        log.info("tmpFolder {}", tmpFolder.getAbsolutePath());

        List<String> tableNames = new ArrayList<String>();
        String url = "jdbc:sqlite:" + fileName;
        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT tablename FROM T_ILI2DB_TABLE_PROP WHERE setting = 'CLASS'")) {
                while(rs.next()) {
                    tableNames.add(rs.getString("tablename"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new IllegalArgumentException(e.getMessage());
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        
        for (String tableName : tableNames) {
            ShapeWriter writer = new ShapeWriter(Paths.get(tmpFolder.getAbsolutePath(), tableName+".shp").toFile());
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
        
        String zipFileName = Paths.get(tmpFolder.getAbsolutePath(), new File(fileName).getName().substring(0, new File(fileName).getName().lastIndexOf(".")) + ".shp.zip").toFile().getAbsolutePath();
        log.info(zipFileName);
        
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFileName));
        Files.walkFileTree(tmpFolder.toPath(), new ZipDir(tmpFolder.toPath(), zos));
        zos.close();
    }
}
