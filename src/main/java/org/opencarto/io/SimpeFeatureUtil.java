/**
 * 
 */
package org.opencarto.io;

import java.util.ArrayList;
import java.util.Collection;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opencarto.datamodel.Feature;
import org.opencarto.util.JTSGeomUtil;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Conversion functions from GT SimpleFeatures from/to OC features
 * 
 * @author julien Gaffuri
 *
 */
public class SimpeFeatureUtil {

	//SimpleFeature to feature
	public static Feature get(SimpleFeature sf, String[] attNames){
		Feature f = new Feature();
		//geom
		f.setGeom(JTSGeomUtil.clean( (Geometry)sf.getProperty("the_geom").getValue() ));
		//attributes
		for(String attName : attNames)
			f.props.put(attName, sf.getProperty(attName).getValue());
		return f;
	}
	public static Feature get(SimpleFeature sf){ return get(sf, getAttributeNames(sf.getFeatureType())); }

	public static ArrayList<Feature> get(SimpleFeatureCollection fs) {
		SimpleFeatureIterator iterator = fs.features();
		ArrayList<Feature> out = new ArrayList<Feature>();
		String[] attNames = getAttributeNames(fs.getSchema());
		int id = 0;
		while( iterator.hasNext()  ){
			Feature f = get(iterator.next(), attNames);
			f.id = ""+id++;
			out.add(f);
		}
		return out;
	}


	//feature to SimpleFeature IRRELEVANT
	//public static SimpleFeature get(Feature f, int epsg){ }
	public static SimpleFeatureCollection get(Collection<Feature> fs) {
		//TODO
		ArrayList<SimpleFeature> sfs = new ArrayList<SimpleFeature>();

		SimpleFeatureType ft = SimpeFeatureUtil.getFeatureType(geomType, epsgCode);
		String geomType = geoms.iterator().next().getGeometryType();
		SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(ft);
		ArrayList<SimpleFeature> out = new ArrayList<SimpleFeature>();
		int id=0;
		for(Geometry geom:geoms)
			out.add( sfb.buildFeature(""+(id++), new Object[]{geom}) );

		return toCollection(sfs);
	}

	public static ArrayList<SimpleFeature> toCollection(SimpleFeatureCollection sfs) {
		ArrayList<SimpleFeature> fs = new ArrayList<SimpleFeature>();
		FeatureIterator<SimpleFeature> it = sfs.features();
		try { while(it.hasNext()) fs.add(it.next()); }
		finally { it.close(); }
		return fs;
	}
	public static SimpleFeatureCollection toCollection(ArrayList<SimpleFeature> fs) {
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



	//feature type
	public static String[] getAttributeNames(SimpleFeatureType sch){
		String[] atts = new String[sch.getAttributeCount()-1];
		for(int i=0; i<sch.getAttributeCount(); i++){
			String att = sch.getDescriptor(i).getLocalName();
			if("the_geom".equals(att)) continue;
			atts[i-1] = att;
		}
		return atts;
	}

}
