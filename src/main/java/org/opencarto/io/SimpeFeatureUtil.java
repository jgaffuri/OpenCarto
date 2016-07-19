/**
 * 
 */
package org.opencarto.io;

import org.geotools.data.DataUtilities;
import org.geotools.feature.SchemaException;
import org.opencarto.datamodel.Feature;
import org.opencarto.util.JTSGeomUtil;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Conversion function from SimpleFeature to Feature
 * 
 * @author julien Gaffuri
 *
 */
public class SimpeFeatureUtil {

	public static Feature get(SimpleFeature sf){
		return get(sf, new String[]{}, "the_geom");
	}

	public static Feature get(SimpleFeature sf, String[] atts){
		return get(sf, atts, "the_geom");
	}

	public static Feature get(SimpleFeature sf, String[] atts, String geomAtt){
		Feature f=new Feature();

		//get geom
		Geometry geom = (Geometry)sf.getProperty("the_geom").getValue();
		f.setGeom(JTSGeomUtil.clean(geom));

		//get attributes
		for(String att : atts)
			f.props.put(att, sf.getProperty(att).getValue());
		
		return f;
	}


	
	
	/*public static SimpleFeature get(Feature f, int epsg){
		return null;
	}*/

	public static SimpleFeatureType getFeatureType(String geomType) {
		return getFeatureType(geomType, -1);
	}
	public static SimpleFeatureType getFeatureType(String geomType, int epsgCode) {
		return getFeatureType(geomType, epsgCode, null);
	}
	public static SimpleFeatureType getFeatureType(String geomType, int epsgCode, String data) {
		//DataUtilities.createType("LINE", "centerline:LineString:srid=32615,name:\"\",id:0");
		//DataUtilities.createType("EDGE", "edge:Polygon,name:String,timestamp:java.util.Date");
		try {
			String st = "";
			st = "GEOM:"+geomType;
			if(epsgCode>0) st += ":srid="+epsgCode;
			if(data!=null) st += ","+data;
			return DataUtilities.createType("ep", st);
		} catch (SchemaException e) {
			e.printStackTrace();
			return null;
		}
	}


}
