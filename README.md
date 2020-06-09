# gpkg2shp-test



```
./gradlew run > foo.log 2>&1
```

```
java -jar /Users/stefan/apps/ili2gpkg-4.4.1/ili2gpkg-4.4.1.jar --dbfile 2502.gpkg --disableValidation --nameByTopic --strokeArcs --createEnumTabs --createMetaInfo --models SO_Nutzungsplanung_20171118 --doSchemaImport --import 2502.xtf

java -jar /Users/stefan/apps/ili2gpkg-4.4.1/ili2gpkg-4.4.1.jar --dbfile ch.so.agi.av-gb-administrative-einteilung.gpkg --disableValidation --nameByTopic --strokeArcs --createEnumTabs --createMetaInfo --models SO_AGI_AV_GB_Administrative_Einteilungen_Publikation_20180822 --doSchemaImport --import ch.so.agi.av-gb-administrative-einteilung.xtf
```


- Endoding: ISO-8859-15. Geprüft in QGIS. UTF-8 ist so pseudo-offiziell.