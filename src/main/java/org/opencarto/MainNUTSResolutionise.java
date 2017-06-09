/**
 * 
 */
package org.opencarto;

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opencarto.algo.resolutionise.Resolutionise;
import org.opencarto.io.ShapeFile;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

/**
 * 
 * Test of NUTS generalisation based on resolutionise operation
 * 
 * @author julien Gaffuri
 *
 */
public class MainNUTSResolutionise {

	public static void main(String[] args) {
		//load nuts regions and boundaries from shapefile
		//ShapeFile rg = new ShapeFile("data/NUTS_2013_01M_SH/NUTS_RG_01M_2013.shp", true);
		ShapeFile bn = new ShapeFile("data/NUTS_2013_01M_SH/NUTS_BN_01M_2013.shp", true);

		double resolution = 0.1;

		//compute generalisation
		FeatureIterator<SimpleFeature> it = bn.getFeatures();
		DefaultFeatureCollection fs = new DefaultFeatureCollection("geneBN", bn.getSchema());
		while(it.hasNext()){
			SimpleFeature f = it.next();
			MultiLineString geom = (MultiLineString) f.getDefaultGeometry();
			if(geom == null) continue;
			Geometry lineal = (Geometry) new Resolutionise(geom, resolution).lineal;
			if(lineal == null ) continue;
			Geometry geom_ = lineal instanceof MultiLineString? lineal : geom.getFactory().createMultiLineString(new LineString[]{(LineString)lineal});
			f.setDefaultGeometry(geom_);
			fs.add(f);
		}

		//save
		ShapeFile shpOut = new ShapeFile(bn.getSchema(), "/home/juju/Bureau/", "out.shp", true, true, true);
		shpOut.add(fs);

		System.out.println("Done");
	}

}
