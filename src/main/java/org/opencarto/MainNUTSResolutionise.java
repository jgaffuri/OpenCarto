/**
 * 
 */
package org.opencarto;

import org.geotools.feature.FeatureIterator;
import org.opencarto.algo.resolutionise.Resolutionise;
import org.opencarto.io.ShapeFile;
import org.opencarto.util.JTSGeomUtil;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;

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

		double resolution = 0.5;
		resolusionise(bn, new ShapeFile(bn.getSchema(), "/home/juju/Bureau/", "out.shp", true, true, true), resolution);

		System.out.println("Done");
	}

	public static void resolusionise(ShapeFile inSHP, ShapeFile outSHP, double resolution){
		//compute generalisation
		FeatureIterator<SimpleFeature> it = inSHP.getFeatures();
		while(it.hasNext()){
			SimpleFeature f = it.next();
			Geometry geom = (Geometry) f.getDefaultGeometry();
			if(geom == null) continue;

			//TODO adapt geometry type
			Geometry geom_ = (Geometry) new Resolutionise(geom, resolution).lineal;
			if(geom_ == null ) continue;

			geom_ = JTSGeomUtil.toMulti(geom_);
			f.setDefaultGeometry(geom_);
			outSHP.add(f);
		}

	}





}
