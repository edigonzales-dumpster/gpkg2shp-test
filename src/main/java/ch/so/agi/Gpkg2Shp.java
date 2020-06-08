package ch.so.agi;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import org.geotools.data.DataStore;
//import org.geotools.data.DataStoreFinder;
//import org.geotools.data.DefaultTransaction;
//import org.geotools.data.Transaction;
//import org.geotools.data.shapefile.ShapefileDataStore;
//import org.geotools.data.shapefile.ShapefileDataStoreFactory;
//import org.geotools.data.simple.SimpleFeatureCollection;
//import org.geotools.data.simple.SimpleFeatureIterator;
//import org.geotools.data.simple.SimpleFeatureSource;
//import org.geotools.data.simple.SimpleFeatureStore;
//import org.geotools.geopkg.GeoPkgDataStoreFactory;
//import org.opengis.feature.simple.SimpleFeature;

public class Gpkg2Shp {
    Logger log = LoggerFactory.getLogger(Gpkg2Shp.class);

    public Gpkg2Shp() {}
    
    public void convert(String fileName) throws IOException {
//        File tmpFolder = Files.createTempDirectory("gpkgws-").toFile();
//        if (!tmpFolder.exists()) {
//            tmpFolder.mkdirs();
//        }
//        log.info("tmpFolder {}", tmpFolder.getAbsolutePath());
//        
//        DataStore store;
//        HashMap<String, Object> map = new HashMap<>();
//        map.put(GeoPkgDataStoreFactory.DBTYPE.key, "geopkg");
//        map.put(GeoPkgDataStoreFactory.DATABASE.key, fileName);
//        try {
//            store = DataStoreFinder.getDataStore(map);
//
//            String[] names = store.getTypeNames();
//
//            for (String name : names) {
//                //log.info(name.toString());
//
//                SimpleFeatureCollection features = store.getFeatureSource(name).getFeatures();
//                
//                ShapefileDataStoreFactory shpDataStoreFactory = new ShapefileDataStoreFactory();
//                Map<String, Serializable> params = new HashMap<>();
//                params.put("url", new File(tmpFolder, name + ".shp").toURI().toURL());
//                params.put("create spatial index", Boolean.TRUE);
//                ShapefileDataStore shpDataStore = (ShapefileDataStore) shpDataStoreFactory.createNewDataStore(params);
//
//                shpDataStore.createSchema(store.getFeatureSource(name).getSchema());
//                String shpTypeName = shpDataStore.getTypeNames()[0];
//                SimpleFeatureSource shpFeatureSource = shpDataStore.getFeatureSource(shpTypeName);
//                SimpleFeatureStore shpFeatureStore = (SimpleFeatureStore) shpFeatureSource;
//                                
//                Transaction transaction = new DefaultTransaction("create");
//                shpFeatureStore.setTransaction(transaction);
//                try {
//                    shpFeatureStore.addFeatures(store.getFeatureSource(name).getFeatures());
//                    transaction.commit();
//                } catch (Exception problem) {
//                    problem.printStackTrace();
//                    transaction.rollback();
//                } finally {
//                    transaction.close();
//                }
//                
//                shpDataStore.dispose();
//                
//                //log.info(store.getFeatureSource(name).getSchema().get);
//                
//                
////                try (SimpleFeatureIterator itr = features.features()) {
////
////                    int count = 0;
////                    while (itr.hasNext() && count < 10) {
////                        SimpleFeature f = itr.next();
//////                        System.out.println(f);
////                        log.info(f.toString());
////                        count++;
////                    }
////                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
