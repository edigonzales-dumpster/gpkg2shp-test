# gpkg2shp-test

```
SELECT 
    table_prop.tablename, 
    gpkg_geometry_columns.column_name,
    gpkg_geometry_columns.srs_id AS crs,
    classname.IliName AS classname,
    attrname.SqlName AS dxf_layer_name
FROM 
    T_ILI2DB_TABLE_PROP AS table_prop
    LEFT JOIN gpkg_geometry_columns
    ON table_prop.tablename = gpkg_geometry_columns.table_name
    LEFT JOIN T_ILI2DB_CLASSNAME AS classname
    ON table_prop.tablename = classname.SqlName 
    LEFT JOIN ( SELECT ilielement, attr_name, attr_value FROM T_ILI2DB_META_ATTRS WHERE attr_name = 'dxflayer' ) AS meta_attrs 
    ON instr(meta_attrs.ilielement, classname) > 0
    LEFT JOIN T_ILI2DB_ATTRNAME AS attrname 
    ON meta_attrs.ilielement = attrname.IliName 
WHERE
    setting = 'CLASS'
    AND 
    column_name IS NOT NULL


    
SELECT 
    table_prop.tablename, 
    gpkg_geometry_columns.column_name,
    gpkg_geometry_columns.srs_id AS crs,
    classname.IliName AS classname
FROM 
    T_ILI2DB_TABLE_PROP AS table_prop
    LEFT JOIN gpkg_geometry_columns
    ON table_prop.tablename = gpkg_geometry_columns.table_name
    LEFT JOIN T_ILI2DB_CLASSNAME AS classname
    ON table_prop.tablename = classname.SqlName 
WHERE
    setting = 'CLASS'
    AND 
    column_name IS NOT NULL
```



```
./gradlew run > foo.log 2>&1
```

```
java -jar /Users/stefan/apps/ili2gpkg-4.4.1/ili2gpkg-4.4.1.jar --dbfile 2502.gpkg --disableValidation --nameByTopic --strokeArcs --createEnumTabs --createMetaInfo --models SO_Nutzungsplanung_20171118 --doSchemaImport --import 2502.xtf

java -jar /Users/stefan/apps/ili2gpkg-4.4.1/ili2gpkg-4.4.1.jar --dbfile ch.so.agi.av-gb-administrative-einteilung.gpkg --disableValidation --nameByTopic --strokeArcs --createEnumTabs --createMetaInfo --models SO_AGI_AV_GB_Administrative_Einteilungen_Publikation_20180822 --doSchemaImport --import ch.so.agi.av-gb-administrative-einteilung.xtf

java -jar /Users/stefan/apps/ili2gpkg-4.4.1/ili2gpkg-4.4.1.jar --dbfile ch.so.agi.av-gb-administrative-einteilung.gpkg --nameByTopic --strokeArcs --disableValidation --createEnumTabs --doSchemaImport --createMetaInfo --models SO_AGI_AV_GB_Administrative_Einteilungen_Publikation_20180822 --modeldir ".;http://models.geo.admin.ch" --import ch.so.agi.av-gb-administrative-einteilung.xtf
```


- Endoding: ISO-8859-15. Geprüft in QGIS. UTF-8 ist so pseudo-offiziell.