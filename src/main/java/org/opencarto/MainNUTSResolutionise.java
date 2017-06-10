/**
 * 
 */
package org.opencarto;

import org.geotools.feature.DefaultFeatureCollection;
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
		ShapeFile rg = new ShapeFile("data/NUTS_2013_01M_SH/NUTS_RG_01M_2013.shp", true);
		ShapeFile bn = new ShapeFile("data/NUTS_2013_01M_SH/NUTS_BN_01M_2013.shp", true);

		double resolution = 0.2;
		resolusionise(bn, new ShapeFile(bn.getSchema(), "/home/juju/Bureau/out/", "bn_"+resolution+".shp", true, true, true), resolution, 1);
		resolusionise(rg, new ShapeFile(bn.getSchema(), "/home/juju/Bureau/out/", "rg_"+resolution+".shp", true, true, true), resolution, 2);

		System.out.println("Done");
	}



	//TODO adapt geometry type
	public static void resolusionise(ShapeFile inSHP, ShapeFile outSHP, double resolution, int type){
		FeatureIterator<SimpleFeature> it = inSHP.getFeatures();
		DefaultFeatureCollection fs = new DefaultFeatureCollection("A"+(Math.random()*1000), inSHP.getSchema());
		while(it.hasNext()){
			SimpleFeature f = it.next();
			Geometry geom = (Geometry) f.getDefaultGeometry();
			if(geom == null) continue;

			Geometry geom_ = Resolutionise.getSimple(geom, resolution);
			//Geometry geom_ = (Geometry)(type==0? new Resolutionise(geom, resolution).puntal : type==1? new Resolutionise(geom, resolution).lineal : new Resolutionise(geom, resolution).polygonal);
			if(geom_ == null ) continue;

			geom_ = JTSGeomUtil.toMulti(geom_);
			f.setDefaultGeometry(geom_);
			fs.add(f);
		}
		outSHP.add(fs);

	}

}
