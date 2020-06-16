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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.interlis2.av2geobau.impl.DxfUtil;
import org.interlis2.av2geobau.impl.DxfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import ch.interlis.iom.IomObject;
import ch.interlis.iom_j.Iom_jObject;
import ch.interlis.iox.IoxEvent;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.ObjectEvent;
import ch.interlis.iox_j.EndBasketEvent;
import ch.interlis.iox_j.EndTransferEvent;
import ch.interlis.iox_j.jts.Iox2jts;
import ch.interlis.iox_j.jts.Jts2iox;
import ch.interlis.ioxwkf.gpkg.GeoPackageReader;

public class Gpkg2Dxf {
    Logger log = LoggerFactory.getLogger(Gpkg2Dxf.class);

    public void convert(String fileName) throws Exception {
        File tmpFolder = Files.createTempDirectory("gpkgws-").toFile();
        if (!tmpFolder.exists()) {
            tmpFolder.mkdirs();
        }
        log.info("tmpFolder {}", tmpFolder.getAbsolutePath());
        
        String sql = "SELECT \n" + 
                "    table_prop.tablename, gpkg_geometry_columns.column_name  \n" + 
                "FROM \n" + 
                "    T_ILI2DB_TABLE_PROP AS table_prop\n" + 
                "    LEFT JOIN gpkg_geometry_columns \n" + 
                "    ON table_prop.tablename = gpkg_geometry_columns.table_name \n" + 
                "WHERE \n" + 
                "    setting = 'CLASS'\n" + 
                "AND \n" + 
                "    column_name IS NOT NULL";
        
        Map<String,String> tableNames = new HashMap<String,String>();
        String url = "jdbc:sqlite:" + fileName;
        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sql)) {
                while(rs.next()) {
                    tableNames.put(rs.getString("tablename"), rs.getString("column_name"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new IllegalArgumentException(e.getMessage());
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        
        java.io.Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/Users/stefan/tmp/fubar.dxf"), "ISO-8859-1")); 
        
        try {
            fw.write(DxfUtil.toString(0, "SECTION"));
            fw.write(DxfUtil.toString(2, "ENTITIES"));
            
            GeoPackageReader reader = new GeoPackageReader(new File(fileName), "nachfuehrngskrise_gemeinde");        
            IoxEvent event = reader.read();
            while (event instanceof IoxEvent) {
                if (event instanceof ObjectEvent) {                
                    ObjectEvent iomObjEvent = (ObjectEvent) event;
                    IomObject iomObj = iomObjEvent.getIomObject();
                    
                    String layer = "fubarlayer";
                    IomObject geom = iomObj.getattrobj("perimeter", 0);
                    
                    // TODO crs aus gpkg
                    // Es kann im Geopackage eine Multisurface vorhanden sein, die
                    // im DxfWriter Probleme macht. Dort wird Iox2jtsext.surface2JTS() 
                    // verwendet.
                    MultiPolygon multipoly = Iox2jts.multisurface2JTS(geom, 0, 2056);                
                    for (int i=0; i<multipoly.getNumGeometries(); i++) {
                        IomObject dxfObj = new Iom_jObject(DxfWriter.IOM_2D_POLYGON, null);
                        dxfObj.setobjectoid(iomObj.getobjectoid());
                        dxfObj.setattrvalue(DxfWriter.IOM_ATTR_LAYERNAME, layer);
                        
                        Polygon poly = (Polygon) multipoly.getGeometryN(i);
                        IomObject surface = Jts2iox.JTS2surface(poly);
                        dxfObj.addattrobj(DxfWriter.IOM_ATTR_GEOM, surface);
                        String dxfFragment = DxfWriter.feature2Dxf(dxfObj);
                        fw.write(dxfFragment);
                    }
                }
                event = reader.read();
            }
                        
            if (reader != null) {
                reader.close();
                reader = null;
            }

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