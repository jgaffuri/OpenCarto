/**
 * 
 */
package org.opencarto;

import org.opencarto.io.GeoJSONUtil;
import org.opencarto.io.SHPUtil;
import org.opencarto.io.SHPUtil.SHPData;

/**
 * @author julien Gaffuri
 *
 */
public class MainGeoflaCommunes {
	private static int epsgCode = 2154;

	private static String outPath = "data/tmp/geofla/";

	public static void main(String[] args) {
		System.out.println("Start");
		SHPData data;

		data = SHPUtil.loadSHP("data/GEOFLA/COMMUNE.shp", epsgCode);
		//for(Feature f : data.fs) f.props.keySet().remove("SHAPE_AREA");
		GeoJSONUtil.toGeoJSON(data.fs, outPath, "COMMUNE.json");

		data = SHPUtil.loadSHP("data/GEOFLA/LIMITE_COMMUNE.shp", epsgCode);
		//for(Feature f : data.fs) f.props.keySet().remove("SHAPE_AREA");
		GeoJSONUtil.toGeoJSON(data.fs, outPath, "LIMITE_COMMUNE.json");

		System.out.println("Done.");
	}

}
