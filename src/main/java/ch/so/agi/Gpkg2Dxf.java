package ch.so.agi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
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

import org.interlis2.av2geobau.impl.DxfUtil;
import org.interlis2.av2geobau.impl.DxfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import ch.interlis.iom.IomObject;
import ch.interlis.iom_j.Iom_jObject;
import ch.interlis.iox.IoxEvent;
import ch.interlis.iox.ObjectEvent;
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
                "    table_prop.tablename, \n" + 
                "    gpkg_geometry_columns.column_name,\n" + 
                "    gpkg_geometry_columns.srs_id AS crs,\n" + 
                "    gpkg_geometry_columns.geometry_type_name AS geometry_type_name,\n" + 
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
        
        List<DxfLayerInfo> dxfLayers = new ArrayList<DxfLayerInfo>();
        String url = "jdbc:sqlite:" + fileName;
        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sql)) {
                while(rs.next()) {
                    DxfLayerInfo dxfLayerInfo = new DxfLayerInfo();
                    dxfLayerInfo.setTableName(rs.getString("tablename"));
                    dxfLayerInfo.setGeomColumnName(rs.getString("column_name"));
                    dxfLayerInfo.setCrs(rs.getInt("crs"));
                    dxfLayerInfo.setGeometryTypeName(rs.getString("geometry_type_name"));
                    dxfLayerInfo.setClassName(rs.getString("classname"));
                    dxfLayerInfo.setDxfLayerAttr(rs.getString("dxf_layer_attr"));
                    dxfLayers.add(dxfLayerInfo);
                    log.info(dxfLayerInfo.getTableName());
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new IllegalArgumentException(e.getMessage());
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
                
        for (DxfLayerInfo dxfLayerInfo : dxfLayers) {
            String tableName = dxfLayerInfo.getTableName();
            String geomColumnName = dxfLayerInfo.getGeomColumnName();
            int crs = dxfLayerInfo.getCrs();
            String geometryTypeName = dxfLayerInfo.getGeometryTypeName();
            String dxfLayerAttr = dxfLayerInfo.getDxfLayerAttr();
            
            String dxfFileName = Paths.get(tmpFolder.getAbsolutePath(), tableName + ".dxf").toFile().getAbsolutePath();
            java.io.Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dxfFileName), "ISO-8859-1")); 
            log.info("dxfFile: " + dxfFileName);
            
            try {
                fw.write(DxfUtil.toString(0, "SECTION"));
                fw.write(DxfUtil.toString(2, "ENTITIES"));
                
                GeoPackageReader reader = new GeoPackageReader(new File(fileName), tableName);        
                IoxEvent event = reader.read();
                while (event instanceof IoxEvent) {
                    if (event instanceof ObjectEvent) {                
                        ObjectEvent iomObjEvent = (ObjectEvent) event;
                        IomObject iomObj = iomObjEvent.getIomObject();
                        
                        String layer;
                        if (dxfLayerAttr != null) {
                            layer = iomObj.getattrvalue(dxfLayerAttr);
                            layer = layer.replaceAll("\\s+","");
                        } else {
                            layer = "default";
                        }
                        IomObject iomGeom = iomObj.getattrobj(geomColumnName, 0);
                        
                        // TODO: Hier braucht es noch mehr... Es gibt ja schliesslich
                        // auch andere Geometrytypen wie Linien und Punkte.
                        Geometry jtsGeom;
                        if (geometryTypeName.toLowerCase().contains("polygon")) {
                            jtsGeom = Iox2jts.multisurface2JTS(iomGeom, 0, crs);
                        } else if (geometryTypeName.toLowerCase().contains("linestring")) {
                            jtsGeom = Iox2jts.multipolyline2JTS(iomGeom, 0);
                        } else if (geometryTypeName.toLowerCase().contains("point")) {
                            jtsGeom = Iox2jts.multicoord2JTS(iomGeom);
                        } else {
                            continue;
                        }
                        
                        
                        
                        // Es kann im Geopackage eine Multisurface vorhanden sein. Diese
                        // macht im DxfWriter Probleme, weil Iox2jtsext.surface2JTS() 
                        // verwendet wird (und nicht multisurface2JTS).
                        MultiPolygon multipoly = Iox2jts.multisurface2JTS(iomGeom, 0, crs);                
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
        
        String zipFileName = Paths.get(tmpFolder.getAbsolutePath(), new File(fileName).getName().substring(0, new File(fileName).getName().lastIndexOf(".")) + ".dxf.zip").toFile().getAbsolutePath();
        log.info(zipFileName);
        
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFileName));
        Files.walkFileTree(tmpFolder.toPath(), new ZipDir(tmpFolder.toPath(), zos));
        zos.close();
    }
}