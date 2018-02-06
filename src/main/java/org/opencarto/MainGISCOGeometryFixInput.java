package org.opencarto;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.opencarto.algo.noding.NodingUtil;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.quadtree.Quadtree;

public class MainGISCOGeometryFixInput {
	private final static Logger LOGGER = Logger.getLogger(MainGISCOGeometryFixInput.class.getName());


	public void makeMultiPolygonValid(String inputFile, String outputPath, String outputFile) {
		ArrayList<Feature> fs = SHPUtil.loadSHP(inputFile).fs;
		for(Feature f : fs) {
			boolean valid = f.getGeom().isValid();
			if(valid) continue;
			LOGGER.warn(f.id + " non valid");
			f.setGeom(f.getGeom().buffer(0));
			f.setGeom(JTSGeomUtil.toMulti(f.getGeom()));
		}
		SHPUtil.saveSHP(fs, outputPath, outputFile);
	}



	public static void main(String[] args) {
		System.out.println("Start");

		String basePath = "/home/juju/Bureau/nuts_gene_data/";
		final String outPath = basePath+"out/";
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
			boolean valid = f.getGeom().isValid();
			if(valid) continue;
			LOGGER.warn(f.id + " non valid");
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





	public static void removeNarrowGapsTesselation(Collection<Feature> units, double nodingResolution) {
		boolean b;

		//build spatial index of all features
		Quadtree index = new Quadtree();
		for(Feature unit : units) index.insert(unit.getGeom().getEnvelopeInternal(), unit);

		//int nb=0;
		//handle units one by one
		for(Feature unit : units) {
			//LOGGER.info(unit.id + " - " + 100.0*(nb++)/units.size());

			//get narrow gaps
			Collection<Polygon> ngs = getNarrowGaps(unit.getGeom(), separationDistanceMeter, quad);

			for(Polygon ng : ngs) {
				ng = (Polygon) ng.buffer(separationDistanceMeter*0.001, quad);
				Geometry newUnitGeom = null;
				try {
					newUnitGeom = unit.getGeom().union(ng);
				} catch (Exception e1) {
					LOGGER.warn("Could not make union of unit "+unit.id+" with gap around " + ng.getCentroid().getCoordinate() + " Exception: "+e1.getClass().getName());
					continue;
				}

				//get units intersecting and correct their geometries
				Collection<Feature> uis = index.query( ng.getEnvelopeInternal() );
				//uis = getTrue(uis, ng.getEnvelopeInternal());
				for(Feature ui : uis) {
					if(ui == unit) continue;
					if(!ui.getGeom().getEnvelopeInternal().intersects(ng.getEnvelopeInternal())) continue;

					Geometry geom_ = null;
					try { geom_ = ui.getGeom().difference(ng); } catch (Exception e) {}
					if(geom_==null || geom_.isEmpty()) {
						LOGGER.trace("Unit "+ui.id+" disappeared when removing gaps of unit "+unit.id+" around "+ng.getCentroid().getCoordinate());
						newUnitGeom = newUnitGeom.difference(ui.getGeom());
						continue;
					} else {
						//set new geometry - update index
						b = index.remove(ui.getGeom().getEnvelopeInternal(), ui);
						if(!b) LOGGER.warn("Could not update index for "+ui.id+" while removing narrow gap of "+unit.id+" around "+ng.getCentroid().getCoordinate());
						ui.setGeom(JTSGeomUtil.toMulti(geom_));
						index.insert(ui.getGeom().getEnvelopeInternal(), ui);
					}
				}

				//set new geometry - update index
				b = index.remove(unit.getGeom().getEnvelopeInternal(), unit);
				if(!b) LOGGER.warn("Could not update index for "+unit.id+" while removing narrow gaps around "+unit.getGeom().getCentroid().getCoordinate());
				unit.setGeom(JTSGeomUtil.toMulti(newUnitGeom));
				index.insert(unit.getGeom().getEnvelopeInternal(), unit);
			}

		}

		if(nodingResolution > 0) {
			LOGGER.trace("Ensure LP noding");
			NodingUtil.fixNoding(units, nodingResolution);
		}
	}


}
