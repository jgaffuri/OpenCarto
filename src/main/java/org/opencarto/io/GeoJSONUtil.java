/**
 * 
 */
package org.opencarto.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.opencarto.datamodel.Feature;
import org.opencarto.util.JTSGeomUtil;
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
	public static void toGeoJSON(Collection<Feature> fs, String outPath, String outFile) { toGeoJSON(SimpleFeatureUtil.get(fs), outPath, outFile); }
	public static void toGeoJSON(Collection<Feature> fs, Writer writer) { toGeoJSON(SimpleFeatureUtil.get(fs), writer); }
	public static void toGeoJSON(SimpleFeatureCollection fc, String outPath, String outFile) {
		try {
			new File(outPath).mkdirs();
			FileWriter fw = new FileWriter(outPath + outFile);
			toGeoJSON(fc, fw);
			fw.close();
		} catch (IOException e) { e.printStackTrace(); }
	}
	public static void toGeoJSON(SimpleFeatureCollection fc, Writer writer) {
		try {
			new FeatureJSON().writeFeatureCollection(fc, writer);
		} catch (IOException e) { e.printStackTrace(); }
	}
	public static void toGeoJSON(String inSHPFilePath, String outPath, String outFile) { toGeoJSON(SHPUtil.getSimpleFeatures(inSHPFilePath), outPath, outFile); }


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

			//get attribute names
			Set<String> propNames = props.get(props.keySet().iterator().next()).keySet();

			//build feature type
			String geomType = geoms.values().iterator().next().getGeometryType();
			SimpleFeatureType ft = SimpleFeatureUtil.getFeatureType(geomType, -1, propNames);

			//build features collection
			DefaultFeatureCollection features = new DefaultFeatureCollection(null,ft);

			//build and add features
			SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(ft);
			for(Entry<String,Geometry> piece : geoms.entrySet())
				for(Geometry geom : JTSGeomUtil.getGeometries(piece.getValue())){
					String id = piece.getKey();
					Object[] atts = new Object[propNames.size()+1];
					atts[0] = geom; int i=1;
					HashMap<String, Object> props_ = props.get(id);
					for(String propName : propNames) atts[i++] = props_.get(propName);
					features.add( sfb.buildFeature(id, atts) );
				}

			//convert featurecollection to geojson
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
