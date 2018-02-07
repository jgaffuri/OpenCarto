package org.opencarto;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.quadtree.Quadtree;

public class MainGISCOGeometryFixInput {
	private final static Logger LOGGER = Logger.getLogger(MainGISCOGeometryFixInput.class.getName());





	public static void main(String[] args) {
		System.out.println("Start");

		String basePath = "/home/juju/Bureau/nuts_gene_data/";
		//final String outPath = basePath+"out/";
		Collection<Feature> fs;

		LOGGER.info("Load data");
		final int epsg = 4258; final String rep="100k_1M/commplus"; fs = SHPUtil.loadSHP(basePath+"commplus/COMM_PLUS_100k.shp", epsg).fs;
		//final int epsg = 3857; final String rep="100k_1M/commplus"; fs = SHPUtil.loadSHP(basePath+"out/100k_1M/commplus/out_narrow_gaps_removed___.shp", epsg).fs;

		for(Feature f : fs)
			if(f.getProperties().get("NUTS_ID") != null) f.id = ""+f.getProperties().get("NUTS_ID");
			else if(f.getProperties().get("COMM_ID") != null) f.id = ""+f.getProperties().get("COMM_ID");
			else if(f.getProperties().get("idgene") != null) f.id = ""+f.getProperties().get("idgene");
			else if(f.getProperties().get("GISCO_ID") != null) f.id = ""+f.getProperties().get("GISCO_ID");

		//make valid
		for(Feature f : fs) {
			LOGGER.info(f.id);
			//boolean valid = f.getGeom().isValid();
			//if(valid) continue;
			//LOGGER.warn(f.id + " non valid");
			f.setGeom(f.getGeom().buffer(0));
		}

		//fix noding issue
		//double nodingResolution = 1e-5;
		//NodingUtil.fixNoding(fs, nodingResolution);

		LOGGER.info("Save");
		for(Feature f : fs) f.setGeom(JTSGeomUtil.toMulti(f.getGeom()));
		SHPUtil.saveSHP(fs, basePath+"commplus/", "COMM_PLUS_100k_valid.shp");

		System.out.println("End");
	}



	
	public void makeMultiPolygonValid(String inputFile, String outputPath, String outputFile) {
		ArrayList<Feature> fs = SHPUtil.loadSHP(inputFile).fs;
		for(Feature f : fs) {
			//boolean valid = f.getGeom().isValid();
			//if(valid) continue;
			//LOGGER.warn(f.id + " non valid");
			f.setGeom(f.getGeom().buffer(0));
			f.setGeom(JTSGeomUtil.toMulti(f.getGeom()));
		}
		SHPUtil.saveSHP(fs, outputPath, outputFile);
	}

	


	public static void ensureTesselation(Collection<Feature> units) {
		boolean b;

		//build spatial index
		Quadtree index = new Quadtree();
		for(Feature unit : units) index.insert(unit.getGeom().getEnvelopeInternal(), unit);

		//int nb=0;
		//handle units one by one
		for(Feature unit : units) {
			//LOGGER.info(unit.id + " - " + 100.0*(nb++)/units.size());

			Geometry geom = unit.getGeom();

			//get units intersecting and correct their geometries
			Collection<Feature> uis = index.query( geom.getEnvelopeInternal() );
			for(Feature ui : uis) {
				if(ui == unit) continue;
				if(!ui.getGeom().getEnvelopeInternal().intersects(geom.getEnvelopeInternal())) continue;

				Geometry geom_ = ui.getGeom().difference(geom);
				if(geom_==null || geom_.isEmpty()) {
					LOGGER.trace("Unit "+ui.id+" disappeared when removing intersection with unit "+unit.id+" around "+ui.getGeom().getCentroid().getCoordinate());
					continue;
				} else {
					//set new geometry - update index
					b = index.remove(ui.getGeom().getEnvelopeInternal(), ui);
					if(!b) LOGGER.warn("Could not update index for "+ui.id+" while removing intersection of "+unit.id+" around "+ui.getGeom().getCentroid().getCoordinate());
					ui.setGeom(JTSGeomUtil.toMulti(geom_));
					index.insert(ui.getGeom().getEnvelopeInternal(), ui);
				}
			}
		}

	}


}
