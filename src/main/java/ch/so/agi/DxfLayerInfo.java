package ch.so.agi;

public class DxfLayerInfo {
    private String tableName;
    
    private String geomColumnName;
    
    private int crs;
    
    private String geometryTypeName;
    
    private String className;
    
    private String dxfLayerAttr;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getGeomColumnName() {
        return geomColumnName;
    }

    public void setGeomColumnName(String geomColumnName) {
        this.geomColumnName = geomColumnName;
    }

    public int getCrs() {
        return crs;
    }

    public void setCrs(int crs) {
        this.crs = crs;
    }

    public String getGeometryTypeName() {
        return geometryTypeName;
    }

    public void setGeometryTypeName(String geometryTypeName) {
        this.geometryTypeName = geometryTypeName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDxfLayerAttr() {
        return dxfLayerAttr;
    }

    public void setDxfLayerAttr(String dxfLayerAttr) {
        this.dxfLayerAttr = dxfLayerAttr;
    }
}
