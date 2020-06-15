package ch.so.agi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.interlis2.av2geobau.impl.DxfUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.interlis.iom.IomObject;
import ch.interlis.iox.IoxEvent;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.ObjectEvent;
import ch.interlis.iox_j.EndBasketEvent;
import ch.interlis.iox_j.EndTransferEvent;
import ch.interlis.ioxwkf.gpkg.GeoPackageReader;

public class Gpkg2Dxf {
    Logger log = LoggerFactory.getLogger(Gpkg2Dxf.class);

    public void convert(String fileName) throws IOException, IoxException {
        File tmpFolder = Files.createTempDirectory("gpkgws-").toFile();
        if (!tmpFolder.exists()) {
            tmpFolder.mkdirs();
        }
        log.info("tmpFolder {}", tmpFolder.getAbsolutePath());
        
        
        // TODO
        // - wie komme ich ans Geometrie-Attribut? -> Meta-Tabellen? / heuristisch ist eher doof.
        // 
        
        
//        List<String> tableNames = new ArrayList<String>();
//        String url = "jdbc:sqlite:" + fileName;
//        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
//            try (ResultSet rs = stmt.executeQuery("SELECT tablename FROM T_ILI2DB_TABLE_PROP WHERE setting = 'CLASS'")) {
//                while(rs.next()) {
//                    log.info(rs.getString("tablename"));
//                    tableNames.add(rs.getString("tablename"));
//                }
//            } catch (SQLException e) {
//                e.printStackTrace();
//                throw new IllegalArgumentException(e.getMessage());
//            }
//        } catch (SQLException e) {
//            throw new IllegalArgumentException(e.getMessage());
//        }
        
        
        GeoPackageReader reader = new GeoPackageReader(new File(fileName), "nachfuehrngskrise_gemeinde");        
        IoxEvent event = reader.read();
        while (event instanceof IoxEvent) {
            if (event instanceof ObjectEvent) {
                //writer.write(event);
                //log.info(event.toString());
                
                ObjectEvent iomObj = (ObjectEvent) event;
                log.info(iomObj.getIomObject().toString());
            }
            event = reader.read();
            //log.info(event.toString());
        }
        
        //writer.write(new EndBasketEvent());
        //writer.write(new EndTransferEvent());

//        if (writer != null) {
//            writer.close();
//            writer = null;
//        }
        
        if (reader != null) {
            reader.close();
            reader = null;
        }

        
        
        
        java.io.Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/Users/stefan/tmp/fubar.dxf"), "ISO-8859-1")); 
        
        try {
            fw.write(DxfUtil.toString(0, "SECTION"));
            fw.write(DxfUtil.toString(2, "ENTITIES"));
            
            
            
            fw.write(DxfUtil.toString(0, "ENDSEC"));
            fw.write(DxfUtil.toString(0, "EOF"));
        } finally{
            if(fw != null) {
                fw.close();
                fw=null;
            }
        }
    }

}