package org.opencarto;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.opencarto.algo.polygon.MorphologicalAnalysis;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.partitionning.Partition;
import org.opencarto.partitionning.Partition.Operation;
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
		final int epsg = 4258; final String rep="100k_1M/commplus"; fs = SHPUtil.loadSHP(basePath+"commplus/COMM_PLUS_100k_valid.shp", epsg).fs;
		//final int epsg = 3857; final String rep="100k_1M/commplus"; fs = SHPUtil.loadSHP(basePath+"out/100k_1M/commplus/out_narrow_gaps_removed___.shp", epsg).fs;

		for(Feature f : fs)
			if(f.getProperties().get("NUTS_ID") != null) f.id = ""+f.getProperties().get("NUTS_ID");
			else if(f.getProperties().get("COMM_ID") != null) f.id = ""+f.getProperties().get("COMM_ID");
			else if(f.getProperties().get("idgene") != null) f.id = ""+f.getProperties().get("idgene");
			else if(f.getProperties().get("GISCO_ID") != null) f.id = ""+f.getProperties().get("GISCO_ID");

		//make valid
		//for(Feature f : fs) f.setGeom(f.getGeom().buffer(0));

		//ensure tesselation
		ensureTesselation(fs);

		//fix noding issue
		//double nodingResolution = 1e-5;
		//NodingUtil.fixNoding(fs, nodingResolution);

		LOGGER.info("Save");
		for(Feature f : fs) f.setGeom(JTSGeomUtil.toMulti(f.getGeom()));
		SHPUtil.saveSHP(fs, basePath+"commplus/", "COMM_PLUS_100k_valid_tess.shp");

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

	public void ensureTesselation(String inputFile, String outputPath, String outputFile) {
		ArrayList<Feature> fs = SHPUtil.loadSHP(inputFile).fs;
		ensureTesselation(fs);
		SHPUtil.saveSHP(fs, outputPath, outputFile);
	}




	public static Collection<Feature> ensureTesselation(Collection<Feature> units) {
		Collection<Feature> out = Partition.runRecursively(new Operation() {
			public void run(Partition p) {
				LOGGER.info(p);
				ensureTesselation_(p.getFeatures());
			}}, units, 3000000, 15000, false);
		return out;
	}

	private static void ensureTesselation_(Collection<Feature> units) {
		boolean b;

		//build spatial index
		Quadtree index = new Quadtree();
		for(Feature unit : units) index.insert(unit.getGeom().getEnvelopeInternal(), unit);

		//int nb=0;
		//handle units one by one
		for(Feature unit : units) {
			//LOGGER.info(unit.id + " - " + 100.0*(nb++)/units.size());

			Geometry g1 = unit.getGeom();

			//get units intersecting and correct their geometries
			Collection<Feature> uis = index.query( g1.getEnvelopeInternal() );
			for(Feature ui : uis) {
				if(ui == unit) continue;
				Geometry g2 = ui.getGeom();
				if(!g2.getEnvelopeInternal().intersects(g2.getEnvelopeInternal())) continue;

				//check overlap
				boolean overlap = false;
				try {
					overlap = g1.overlaps(g2);
				} catch (Exception e) {
					//overlaps.add(new Overlap(unit.id, null, -1, -1));
					continue;
				}
				if(!overlap) continue;

				Geometry inter = g2.intersection(g1);
				double interArea = inter.getArea();
				if(interArea == 0) continue;

				g2 = g2.difference(g1);
				if(g2==null || g2.isEmpty()) {
					LOGGER.trace("Unit "+ui.id+" disappeared when removing intersection with unit "+unit.id+" around "+ui.getGeom().getCentroid().getCoordinate());
					continue;
				} else {
					//set new geometry - update index
					b = index.remove(ui.getGeom().getEnvelopeInternal(), ui);
					if(!b) LOGGER.warn("Could not update index for "+ui.id+" while removing intersection of "+unit.id+" around "+ui.getGeom().getCentroid().getCoordinate());
					ui.setGeom(JTSGeomUtil.toMulti(g2));
					index.insert(ui.getGeom().getEnvelopeInternal(), ui);
				}
			}
		}

	}


}
