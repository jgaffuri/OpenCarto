/**
 * 
 */
package org.opencarto.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.opencarto.io.geojson.MfFeature;
import org.opencarto.io.geojson.MfFeatureCollection;
import org.opencarto.io.geojson.MfGeo;
import org.opencarto.io.geojson.MfGeoFactory;
import org.opencarto.io.geojson.MfGeoJSONReader;
import org.opencarto.io.geojson.MfGeoJSONWriter;
import org.opencarto.io.geojson.MfGeometry;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author julien Gaffuri
 *
 */
public class GeoJSONUtil {

	public static String convert(HashMap<String,Geometry> geoms, HashMap<String,HashMap<String,Object>> props){
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
	}


	//save a as geojson file
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


	//build mf feature for geojson export
	private static MfFeature getGeoJSONFeature(final String id, final Geometry geom, final HashMap<String,Object> props){
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
	}



	//load a geojson geometry
	public static Geometry loadGeom(String geomGeoJSON){
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
	}

}
