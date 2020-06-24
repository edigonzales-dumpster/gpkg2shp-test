package ch.so.agi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import ch.interlis.iom.IomObject;
import ch.interlis.iom_j.Iom_jObject;
import ch.interlis.iom_j.csv.CsvWriter;
import ch.interlis.iox.IoxEvent;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.ObjectEvent;
import ch.interlis.iox_j.EndBasketEvent;
import ch.interlis.iox_j.EndTransferEvent;
import ch.interlis.iox_j.jts.Iox2jts;
import ch.interlis.iox_j.jts.Iox2jtsException;
import ch.interlis.ioxwkf.gpkg.GeoPackageReader;

public class Gpkg2Csv {
    Logger log = LoggerFactory.getLogger(Gpkg2Csv.class);

    private static final String COORD="COORD";
    private static final String MULTICOORD="MULTICOORD";
    private static final String POLYLINE="POLYLINE";
    private static final String MULTIPOLYLINE="MULTIPOLYLINE";
    private static final String MULTISURFACE="MULTISURFACE";

    public void convert(String fileName) throws IOException, IoxException, Iox2jtsException {
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

        GeometryFactory geometryFactory = new GeometryFactory();

        for (String tableName : tableNames) {
            CsvWriter writer = new CsvWriter(Paths.get(tmpFolder.getAbsolutePath(), tableName+".csv").toFile());
            GeoPackageReader reader = new GeoPackageReader(new File(fileName), tableName);  
            
            IoxEvent event = reader.read();
            while (event instanceof IoxEvent) {

                if (event instanceof ObjectEvent) {                
                    ObjectEvent iomObjEvent = (ObjectEvent) event;
                    IomObject iomObj = iomObjEvent.getIomObject();
                    
                    IomObject csvObj = new Iom_jObject(new File(fileName).getName()+".Topic.Class", null);
                    csvObj.setattrvalue("t_id", iomObj.getobjectoid());

                    String[] attrs = reader.getAttributes();
                    for (String attr : attrs) {
                        if (reader.getGeometryAttributes().contains(attr)) {
                            IomObject iomGeom = iomObj.getattrobj(attr, 0);
                            
                            if (iomGeom != null) {
                                Geometry jtsGeom;
                                if (iomGeom.getobjecttag().equals(MULTISURFACE)) {
                                    jtsGeom = Iox2jts.multisurface2JTS(iomGeom, 0, -1); // crs will not be published in csv 
                                } else if (iomGeom.getobjecttag().equals(MULTIPOLYLINE)) {
                                    jtsGeom = Iox2jts.multipolyline2JTS(iomGeom, 0);
                                } else if (iomGeom.getobjecttag().equals(MULTICOORD)) {
                                    jtsGeom = Iox2jts.multicoord2JTS(iomGeom);
                                } else if (iomGeom.getobjecttag().equals(POLYLINE)) {
                                    CoordinateList coordList = Iox2jts.polyline2JTS(iomGeom, false, 0);
                                    Coordinate[] coordArray = new Coordinate[coordList.size()];
                                    coordArray = (Coordinate[]) coordList.toArray(coordArray);
                                    jtsGeom = geometryFactory.createLineString(coordArray);
                                } else if (iomGeom.getobjecttag().equals(COORD)) {
                                    Coordinate coord = Iox2jts.coord2JTS(iomGeom);
                                    jtsGeom = geometryFactory.createPoint(coord);
                                } else {
                                    continue;
                                }
                                csvObj.setattrvalue(attr, jtsGeom.toString());
                            }
                        } else {
                            String value = iomObj.getattrvalue(attr); 
                            if (value!=null) {
                                csvObj.setattrvalue(attr, value);
                            } else {
                                csvObj.setattrvalue(attr, "");
                            }
                        }
                    }          
                    ch.interlis.iox_j.ObjectEvent csvEvent = new ch.interlis.iox_j.ObjectEvent(csvObj);
                    writer.write(csvEvent);
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
}
