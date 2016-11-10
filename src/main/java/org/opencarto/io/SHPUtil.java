package org.opencarto.io;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opencarto.datamodel.Feature;
import org.opencarto.util.JTSGeomUtil;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author julien Gaffuri
 *
 */
public class SHPUtil {

	//get basic info on shp file

	public static SimpleFeatureType getSchema(String shpFilePath){
		return getSimpleFeatures(shpFilePath).getSchema();
	}
	public static String[] getAttributeNames(String shpFilePath){
		return SimpleFeatureUtil.getAttributeNames(getSchema(shpFilePath));
	}
	public static CoordinateReferenceSystem getCRS(String shpFilePath){
		return getSchema(shpFilePath).getCoordinateReferenceSystem();
	}
	public static Envelope getBounds(String shpFilePath) {
		return getSimpleFeatures(shpFilePath).getBounds();
	}


	//load

	public static SimpleFeatureCollection getSimpleFeatures(String shpFilePath){ return getSimpleFeatures(shpFilePath, null); }
	public static SimpleFeatureCollection getSimpleFeatures(String shpFilePath, Filter f){
		try {
			FileDataStore store = FileDataStoreFinder.getDataStore(new File(shpFilePath));
			DefaultFeatureCollection sfs = DataUtilities.collection(store.getFeatureSource().getFeatures(f));
			store.dispose();
			return sfs;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static class SHPData{
		public SimpleFeatureType ft;
		public ArrayList<Feature> fs;
		public ReferencedEnvelope env;
		public SHPData(SimpleFeatureType ft, ArrayList<Feature> fs, ReferencedEnvelope env){
			this.ft=ft; this.fs=fs; this.env=env;
		}
	}

	public static SHPData loadSHP(String shpFilePath) { return loadSHP(shpFilePath, -1); }
	public static SHPData loadSHP(String shpFilePath, int epsgCode) { return loadSHP(shpFilePath, epsgCode, null); }
	public static SHPData loadSHP(String shpFilePath, int epsgCode, Filter f) {
		SimpleFeatureCollection sfs = getSimpleFeatures(shpFilePath, f);
		return new SHPData(sfs.getSchema(), SimpleFeatureUtil.get(sfs, epsgCode), sfs.getBounds());
	}





	//save

	public static void saveSHP(Collection<Feature> fs, String outPath, String outFile) { saveSHP(SimpleFeatureUtil.get(fs), outPath, outFile); }
	public static void saveSHP(SimpleFeatureCollection sfs, String outPath, String outFile) {
		try {
			//create output file
			new File(outPath).mkdirs();
			File file = new File(outPath+outFile);
			if(file.exists()) file.delete();

			//create feature store
			HashMap<String, Serializable> params = new HashMap<String, Serializable>();
			params.put("url", file.toURI().toURL());
			params.put("create spatial index", Boolean.TRUE);
			ShapefileDataStore ds = (ShapefileDataStore) new ShapefileDataStoreFactory().createNewDataStore(params);
			ds.createSchema(sfs.getSchema());
			String tn = ds.getTypeNames()[0];
			SimpleFeatureStore fst = (SimpleFeatureStore)ds.getFeatureSource(tn);

			//creation transaction
			Transaction tr = new DefaultTransaction("create");
			fst.setTransaction(tr);
			try {
				fst.addFeatures(sfs);
				tr.commit();
			} catch (Exception e) {
				e.printStackTrace();
				tr.rollback();
			} finally {
				tr.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void saveGeomsSHP(Collection<Geometry> geoms, int epsgCode, String outPath, String outFile) {
		try {
			ArrayList<Feature> fs = new ArrayList<Feature>();
			for(Geometry geom : geoms){
				Feature f = new Feature();
				f.setGeom(geom);
				f.setProjCode(epsgCode);
				fs.add(f);
			}
			saveSHP(SimpleFeatureUtil.get(fs), outPath, outFile);
		} catch (Exception e) { e.printStackTrace(); }
	}

	public static void saveGeomsSHP(Collection<Geometry> geoms, String outPath, String outFile) {
		saveGeomsSHP(geoms, -1, outPath, outFile);
	}




	//add feature to a shapefile
	private static void add(SimpleFeature f, String inFile) {
		try {
			Map<String,URL> map = new HashMap<String,URL>();
			map.put("url", new File(inFile).toURI().toURL());
			DataStore ds = DataStoreFinder.getDataStore(map);
			String typeName = ds.getTypeNames()[0];
			SimpleFeatureType ft = ds.getFeatureSource(typeName).getFeatures().getSchema();

			Transaction tr = new DefaultTransaction("create");
			String tn = ds.getTypeNames()[0];
			SimpleFeatureSource fs_ = ds.getFeatureSource(tn);

			if (fs_ instanceof SimpleFeatureStore) {
				SimpleFeatureStore fst = (SimpleFeatureStore) fs_;

				DefaultFeatureCollection objs = new DefaultFeatureCollection(null, ft);
				objs.add(f);

				fst.setTransaction(tr);
				try {
					fst.addFeatures(objs);
					tr.commit();
				} catch (Exception problem) {
					problem.printStackTrace();
					tr.rollback();
				} finally {
					tr.close();
				}
			} else {
				System.out.println(tn + " does not support read/write access");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}







	//remove empty or null geometries from collection
	public static void removeNullOrEmpty(Collection<SimpleFeature> fs, String geomAtt) {
		ArrayList<SimpleFeature> toRemove = new ArrayList<SimpleFeature>();
		for(SimpleFeature f:fs){
			Geometry g = (Geometry)f.getAttribute(geomAtt);
			if(g==null || g.isEmpty())
				toRemove.add(f);
		}
		fs.removeAll(toRemove);
	}

	//clean geometries of a shapefile
	public static void cleanGeometries(String inFile, String geomAtt, String outPath, String outFile){
		System.out.println("Load data from "+inFile);
		SHPData data = loadSHP(inFile);

		System.out.print("clean all geometries...");
		for(Feature f : data.fs)
			f.setGeom( JTSGeomUtil.toMulti(JTSGeomUtil.clean( f.getGeom() )));
		System.out.println(" Done.");

		System.out.println("Save data to "+outFile);
		saveSHP(SimpleFeatureUtil.get(data.fs), outPath, outFile);
	}

	//save the union of a shapefile into another one
	public static void union(String inFile, String geomAtt, String outPath, String outFile){
		try {
			//load input shp
			SHPData data = loadSHP(inFile);

			//build union
			ArrayList<Geometry> geoms = new ArrayList<Geometry>();
			for( Feature f : data.fs )
				geoms.add(f.getGeom());
			Geometry union = JTSGeomUtil.unionPolygons(geoms);

			System.out.println(union.getGeometryType());

			//build feature
			SimpleFeatureBuilder fb = new SimpleFeatureBuilder(DataUtilities.createType("ep", "the_geom:"+union.getGeometryType()));
			fb.add(union);
			SimpleFeature sf = fb.buildFeature(null);

			//save shp
			DefaultFeatureCollection outfc = new DefaultFeatureCollection(null,null);
			outfc.add(sf);
			saveSHP(SimpleFeatureUtil.get(data.fs), outPath, outFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//get geometrical difference of a shapefile
	public static Geometry getDifferenceGeom(String inFile, String geomAtt, double margin) {
		//load input shp
		SHPData data = loadSHP(inFile);

		//get envelope
		Envelope e=data.env;
		e.expandBy(margin, margin);
		Geometry diff = JTSGeomUtil.getGeometry(e);
		e=null;

		//get poly list
		ArrayList<Geometry> polys = new ArrayList<Geometry>();
		for( Feature f:data.fs )
			polys.add(f.getGeom());

		//get union
		Geometry union = JTSGeomUtil.unionPolygons(polys);
		polys=null;

		//compute difference
		diff = diff.difference(union);
		if(diff instanceof Polygon || diff instanceof MultiPolygon) return diff;
		union=null;
		return diff.buffer(0);
	}


	//get geometrical difference of a shapefile
	public static SimpleFeature getDifferenceFeature(String inFile, String geomAtt, double margin) {
		try {
			//build difference geometry
			Geometry comp = getDifferenceGeom(inFile, geomAtt, margin);

			//get feature type
			Map<String,URL> map = new HashMap<String,URL>();
			map.put("url", new File(inFile).toURI().toURL());
			DataStore ds = DataStoreFinder.getDataStore(map);
			String typeName = ds.getTypeNames()[0];
			SimpleFeatureType ft = ds.getFeatureSource(typeName).getFeatures().getSchema();

			//build feature
			SimpleFeatureBuilder fb;
			fb = new SimpleFeatureBuilder(ft);
			fb.add(comp);
			return fb.buildFeature(null);
		} catch (Exception e) { e.printStackTrace(); }
		return null;
	}

	public static void addDifference(String inFile, String geomAtt, double margin) {
		add(getDifferenceFeature(inFile,geomAtt,margin), inFile);
	}


	/*public static void main(String[] args) {
		System.out.println( getCRS("data/CNTR_2014_03M_SH/CNTR_RG_03M_2014.shp") );
	}*/

}
