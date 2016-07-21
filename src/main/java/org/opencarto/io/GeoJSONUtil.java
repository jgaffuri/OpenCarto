/**
 * 
 */
package org.opencarto.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.opencarto.util.JTSGeomUtil;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author julien Gaffuri
 *
 */
public class GeoJSONUtil {

	/**
	 * Convert a SHP file into a geoJSON file
	 * 
	 * @param inSHPFilePath
	 * @param outGeoJSONFilePath
	 */
	public static void toGeoJSON(String inSHPFilePath, String outGeoJSONFilePath) {
		try {
			FileWriter fw = new FileWriter(outGeoJSONFilePath);
			toGeoJSON(inSHPFilePath, fw);
			fw.close();
		} catch (IOException e) { e.printStackTrace(); }
	}
	public static void toGeoJSON(String inSHPFilePath, Writer writer) {
		try {
			new FeatureJSON().writeFeatureCollection(SHPUtil.getSimpleFeatures(inSHPFilePath), writer);
		} catch (IOException e) { e.printStackTrace(); }
	}


	/**
	 * @param geoms
	 * @param props
	 * @param folderPath
	 * @param fileName
	 * @return
	 */
	public static boolean save(HashMap<String,Geometry> geoms, HashMap<String,HashMap<String,Object>> props, String folderPath, String fileName){
		try {
			if(geoms.size()==0){
				System.out.println("Nothing to convert in geoJSON !");
				return false;
			}

			String geomType = geoms.values().iterator().next().getGeometryType();
			//TODO
			System.err.println("Warning: Complete code in GeoJSONUtil.convert !!!");
			String data = null;
			SimpleFeatureType ft = SimpeFeatureUtil.getFeatureType(geomType, -1, data);
			SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(ft);
			ArrayList<SimpleFeature> out = new ArrayList<SimpleFeature>();
			for(Entry<String,Geometry> piece : geoms.entrySet())
				for(Geometry geom : JTSGeomUtil.getGeometries(piece.getValue())){
					//String id = piece.getKey();
					//geom
					//props: props.get(piece.getKey())
					//TODO
					out.add( sfb.buildFeature(piece.getKey(), new Object[]{geom}) );
				}

			//build feature collection
			DefaultFeatureCollection features = new DefaultFeatureCollection(null,ft);
			for(SimpleFeature f : out) features.add(f);

			StringWriter writer = new StringWriter();
			new FeatureJSON().writeFeatureCollection(features, writer);
			String gjson = writer.toString();

			if(gjson==null) return false;
			new File(folderPath).mkdirs();
			FileWriter w = new FileWriter(new File(folderPath+File.separator+fileName));
			w.write(gjson);
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
