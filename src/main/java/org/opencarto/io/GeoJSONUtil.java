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


	public static String convert(HashMap<String,Geometry> geoms, HashMap<String,HashMap<String,Object>> props){
		try {
			if(geoms.size()==0){
				System.out.println("Nothing to convert in geoJSON !");
				return null;
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
			return writer.toString();
		} catch (IOException e) { e.printStackTrace(); return null; }
	}

	public static boolean save(HashMap<String,Geometry> geoms, HashMap<String,HashMap<String,Object>> props, String folderPath, String fileName){
		String out = convert(geoms, props);

		if(out==null) return false;
		new File(folderPath).mkdirs();
		try {
			FileWriter w = new FileWriter(new File(folderPath+File.separator+fileName));
			w.write(out);
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}



	/*public static String convert(HashMap<String,Geometry> geoms, HashMap<String,HashMap<String,Object>> props){
		if(geoms.size()==0){
			System.out.println("Nothing to convert in geoJSON !");
			return null;
		}

		ArrayList<MfFeature> features = new ArrayList<MfFeature>();
		for(Entry<String,Geometry> piece : geoms.entrySet())
			for(Geometry geom : JTSGeomUtil.getGeometries(piece.getValue()))
				features.add(getGeoJSONFeature(piece.getKey(), geom, props.get(piece.getKey())));

		StringWriter w = new StringWriter();
		MfGeoJSONWriter gjw = new MfGeoJSONWriter(new JSONWriter(w));
		try {
			gjw.encodeFeatureCollection(new MfFeatureCollection(features));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return w.toString();
	}*/

	//save a as geojson file
	/*public static boolean save(HashMap<String,Geometry> geoms, HashMap<String,HashMap<String,Object>> props, String folderPath, String fileName){
		String out = convert(geoms, props);
		if(out==null) return false;
		new File(folderPath).mkdirs();
		try {
			FileWriter w = new FileWriter(new File(folderPath+File.separator+fileName));
			w.write(out);
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}*/


	//build mf feature for geojson export
	/*private static MfFeature getGeoJSONFeature(final String id, final Geometry geom, final HashMap<String,Object> props){
		MfFeature f=new MfFeature() {
			@Override
			public void toJSON(JSONWriter builder) {
				if(props==null) return;
				if(props.size() == 0) return;
				Set<String> keys = props.keySet();
				for(String key : keys){
					Object att = props.get(key);
					try {
						builder.key(key);
						builder.value(att);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
			@Override
			public MfGeometry getMfGeometry() {
				if(geom==null) return null;
				return new MfGeometry(geom);
			}
			@Override
			public String getFeatureId() { return id; }
		};
		return f;
	}*/



	//load a geojson geometry
	/*public static Geometry loadGeom(String geomGeoJSON){
		try {
			MfGeoJSONReader r = new MfGeoJSONReader(new MfGeoFactory() {
				@Override
				public MfFeature createFeature(String id, MfGeometry geometry, JSONObject properties) {
					return null;
				}
			});
			MfGeo dec = r.decode(geomGeoJSON);
			return ((MfGeometry)dec).getInternalGeometry();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}*/

}
