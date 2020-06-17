package ch.so.agi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.interlis.iom.IomObject;
import ch.interlis.iox.IoxEvent;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.ObjectEvent;
import ch.interlis.ioxwkf.gpkg.GeoPackageReader;

public class Gpkg2Csv {
    Logger log = LoggerFactory.getLogger(Gpkg2Dxf.class);

    public void convert(String fileName) throws IOException, IoxException {
        File tmpFolder = Files.createTempDirectory("gpkgws-").toFile();
        if (!tmpFolder.exists()) {
            tmpFolder.mkdirs();
        }
        log.info("tmpFolder {}", tmpFolder.getAbsolutePath());
        
        
        String sql = "SELECT \n" + 
                "    table_prop.tablename, \n" + 
                "    gpkg_geometry_columns.column_name,\n" + 
                "    gpkg_geometry_columns.srs_id AS crs,\n" + 
                "    classname.IliName AS classname,\n" + 
                "    attrname.SqlName AS dxf_layer_attr\n" + 
                "FROM \n" + 
                "    T_ILI2DB_TABLE_PROP AS table_prop\n" + 
                "    LEFT JOIN gpkg_geometry_columns\n" + 
                "    ON table_prop.tablename = gpkg_geometry_columns.table_name\n" + 
                "    LEFT JOIN T_ILI2DB_CLASSNAME AS classname\n" + 
                "    ON table_prop.tablename = classname.SqlName \n" + 
                "    LEFT JOIN ( SELECT ilielement, attr_name, attr_value FROM T_ILI2DB_META_ATTRS WHERE attr_name = 'dxflayer' ) AS meta_attrs \n" + 
                "    ON instr(meta_attrs.ilielement, classname) > 0\n" + 
                "    LEFT JOIN T_ILI2DB_ATTRNAME AS attrname \n" + 
                "    ON meta_attrs.ilielement = attrname.IliName \n" + 
                "WHERE\n" + 
                "    setting = 'CLASS'\n" + 
                "    AND \n" + 
                "    column_name IS NOT NULL";
        
        Map<String,String> layerMap = new HashMap<String,String>();
        String url = "jdbc:sqlite:" + fileName;
        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sql)) {
                while(rs.next()) {
                    layerMap.put(rs.getString("tablename"), rs.getString("column_name"));
                    log.info(layerMap.toString());
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new IllegalArgumentException(e.getMessage());
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        for (Map.Entry<String, String> entry : layerMap.entrySet()) {
            String tableName = entry.getKey();
            String geomColumnName = entry.getValue();

            GeoPackageReader reader = new GeoPackageReader(new File(fileName), tableName);        
            IoxEvent event = reader.read();
            while (event instanceof IoxEvent) {
                if (event instanceof ObjectEvent) {                
                    ObjectEvent iomObjEvent = (ObjectEvent) event;
                    IomObject iomObj = iomObjEvent.getIomObject();
                    
                    

                }
                
                event = reader.read();
            }
            if (reader != null) {
                reader.close();
                reader = null;
            }            
        }
    }
}
