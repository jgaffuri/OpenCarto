/**
 * 
 */
package org.opencarto;

import java.util.ArrayList;

import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;

/**
 * @author julien Gaffuri
 *
 */
public class MainNUTS {


	public static void main(String[] args) {
		String inPath = "data/NUTS_2013_01M_SH/NUTS_RG_01M_2013.shp";
		String outPath = "H:/desktop/nuts/";

		//System.out.println( SHPUtil.getSchema(inPath) );
		//the_geom:MultiPolygon   NUTS_ID:NUTS_ID   STAT_LEVL_:STAT_LEVL_   SHAPE_AREA:SHAPE_AREA   SHAPE_LEN:SHAPE_LEN

		//load
		System.out.println("Load nuts in "+inPath);
		ArrayList<Feature> fs = SHPUtil.loadShp(inPath);
		System.out.println(fs.size() + " nuts loaded.");
		/*
		System.out.println("Save as geojson");
		GeoJSONUtil.toGeoJSON(fs, outPath);*/

		System.out.println("Done.");
	}

}
