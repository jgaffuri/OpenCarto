/**
 * 
 */
package org.opencarto.io;

import java.util.Collection;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
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

	//SimpleFeature to feature
	public static Feature get(SimpleFeature sf, String[] atts, String geomAtt){
		Feature f = new Feature();
		//geom
		f.setGeom(JTSGeomUtil.clean( (Geometry)sf.getProperty("the_geom").getValue() ));
		//attributes
		for(String att : atts) f.props.put(att, sf.getProperty(att).getValue());
		return f;
	}
	public static Feature get(SimpleFeature sf){ return get(sf, new String[]{}, "the_geom"); }
	public static Feature get(SimpleFeature sf, String[] atts){ return get(sf, atts, "the_geom"); }
	public static Collection<Feature> get(SimpleFeatureCollection fs) {
		//TODO retrieve from SHP loader?
		return null;
	}



	//feature to SimpleFeature
	public static SimpleFeature get(Feature f, int epsg){
		//TODO
		return null;
	}
	public static SimpleFeatureCollection get(Collection<Feature> fs) {
		//TODO
		/*/build feature type
		SimpleFeatureType ft = SimpeFeatureUtil.getFeatureType(geomType, -1, data);

		//build features collection
		DefaultFeatureCollection features = new DefaultFeatureCollection(null,ft);
		 */
		return null;
	}






	public static SimpleFeatureType getFeatureType(String geomType) {
		return getFeatureType(geomType, -1);
	}
	public static SimpleFeatureType getFeatureType(String geomType, int epsgCode) {
		return getFeatureType(geomType, epsgCode, null);
	}
	public static SimpleFeatureType getFeatureType(String geomType, int epsgCode, String data) {
		//TODO improve data parameter
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
