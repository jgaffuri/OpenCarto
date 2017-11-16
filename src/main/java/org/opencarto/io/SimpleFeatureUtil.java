/**
 * 
 */
package org.opencarto.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opencarto.datamodel.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Conversion functions from GT SimpleFeatures from/to OC features
 * 
 * @author julien Gaffuri
 *
 */
public class SimpleFeatureUtil {
	//private final static Logger LOGGER = Logger.getLogger(SimpleFeatureUtil.class);

	//SimpleFeature to feature
	public static Feature get(SimpleFeature sf, String[] attNames, int epsgCode){
		Feature f = new Feature();
		//geom
		//f.setGeom(JTSGeomUtil.clean( (Geometry)sf.getProperty("the_geom").getValue() ));
		f.setGeom( (Geometry)sf.getProperty("the_geom").getValue() );
		//attributes
		for(String attName : attNames) f.getProperties().put(attName, sf.getProperty(attName).getValue());
		f.setProjCode(epsgCode);
		return f;
	}
	public static Feature get(SimpleFeature sf, String[] attNames){ return get(sf, getAttributeNames(sf.getFeatureType()), -1); }
	public static Feature get(SimpleFeature sf){ return get(sf, getAttributeNames(sf.getFeatureType())); }

	public static ArrayList<Feature> get(SimpleFeatureCollection sfs) { return get(sfs,-1); }
	public static ArrayList<Feature> get(SimpleFeatureCollection sfs, int epsgCode) {
		SimpleFeatureIterator it = sfs.features();
		ArrayList<Feature> fs = new ArrayList<Feature>();
		String[] attNames = getAttributeNames(sfs.getSchema());
		while( it.hasNext()  ) {
			fs.add(get(it.next(), attNames, epsgCode));
		}
		return fs;
	}


	//feature to SimpleFeature
	public static SimpleFeature get(Feature f){ return get(f, getFeatureType(f)); }
	public static SimpleFeature get(Feature f, SimpleFeatureType ft){
		String[] attNames = getAttributeNames(ft);
		Object[] atts = new Object[attNames.length+1];
		atts[0] = f.getGeom();
		for(int i=0; i<attNames.length; i++) atts[i+1] = f.getProperties().get(attNames[i]);
		return new SimpleFeatureBuilder(ft).buildFeature(f.id, atts);
	}
	public static SimpleFeatureCollection get(Collection<Feature> fs) {
		SimpleFeatureType ft = fs.size()==0? null : getFeatureType(fs.iterator().next());
		DefaultFeatureCollection sfc = new DefaultFeatureCollection(null, ft);
		if(fs.size() > 0){
			SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(ft);
			String[] attNames = getAttributeNames(ft);
			for(Feature f:fs){
				Object[] atts = new Object[attNames.length+1];
				atts[0] = f.getGeom();
				for(int i=0; i<attNames.length; i++) atts[i+1] = f.getProperties().get(attNames[i]);
				sfc.add( sfb.buildFeature(f.id, atts) );
			}
		}
		return sfc;
	}


	/*public static ArrayList<SimpleFeature> toCollection(SimpleFeatureCollection sfs) {
		ArrayList<SimpleFeature> fs = new ArrayList<SimpleFeature>();
		FeatureIterator<SimpleFeature> it = sfs.features();
		try { while(it.hasNext()) fs.add(it.next()); }
		finally { it.close(); }
		return fs;
	}
	public static SimpleFeatureCollection toCollection(Collection<SimpleFeature> sfs) {
		DefaultFeatureCollection sfc = new DefaultFeatureCollection(null, sfs.iterator().next().getFeatureType());
		for(SimpleFeature sf:sfs) sfc.add(sf);
		return sfc;
	}*/



	private static SimpleFeatureType getFeatureType(Feature f) {
		//System.out.println( f.getGeom().getGeometryType() );
		return getFeatureType( f.getGeom().getGeometryType(), f.getProjCode(), f.getProperties().keySet() );
	}
	public static SimpleFeatureType getFeatureType(String geomType) {
		return getFeatureType(geomType, -1);
	}
	public static SimpleFeatureType getFeatureType(String geomType, int epsgCode) {
		return getFeatureType(geomType, epsgCode, new String[]{});
	}
	public static SimpleFeatureType getFeatureType(String geomType, int epsgCode, Collection<String> data) {
		return getFeatureType(geomType, epsgCode, data.toArray(new String[data.size()]));
	}
	public static SimpleFeatureType getFeatureType(String geomType, int epsgCode, String[] data) {
		String datast = "";
		if(data!=null) for(String data_ : data) datast += ","+data_;
		return getFeatureType(geomType, epsgCode, datast.substring(1, datast.length()));
	}
	public static SimpleFeatureType getFeatureType(String geomType, int epsgCode, String data) {
		try {
			String st = "";
			st = "the_geom:"+geomType;
			if(epsgCode>0) st += ":srid="+epsgCode;
			if(data!=null) st += ","+data;
			return DataUtilities.createType("ep", st);
		} catch (SchemaException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String[] getAttributeNames(SimpleFeatureType sch){
		Collection<String> atts = new HashSet<String>();
		for(int i=0; i<sch.getAttributeCount(); i++){
			String att = sch.getDescriptor(i).getLocalName();
			if("the_geom".equals(att)) continue;
			if("GEOM".equals(att)) continue;
			atts.add(att);
		}
		return atts.toArray(new String[atts.size()]);
	}

	/*public static void main(String[] args) {
		Feature f = new Feature(); f = new Feature(); f = new Feature();
		f.props.put("type", "lalala");
		f.props.put("truc", "pspsps");
		f.setGeom(new GeometryFactory().createPoint(new Coordinate(15,48)));
		System.out.println(f.id);
		System.out.println(getFeatureType(f));
	}*/

}
