package org.opencarto.gisco;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.geotools.geometry.jts.JTS;
import org.opencarto.algo.noding.NodingUtil;
import org.opencarto.algo.noding.NodingUtil.NodingIssueType;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.partitionning.Partition;
import org.opencarto.partitionning.Partition.Operation;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion;

public class MainGISCOGeometryFixInput {
	private final static Logger LOGGER = Logger.getLogger(MainGISCOGeometryFixInput.class.getName());


	public static void main(String[] args) {
		System.out.println("Start");

		String basePath = "/home/juju/Bureau/nuts_gene_data/";

		LOGGER.info("Load data");
		int epsg = 4258; Collection<Feature> fs = SHPUtil.loadSHP(basePath+"nutsplus/NUTS_PLUS_01M_1403.shp", epsg).fs;
		//int epsg = 4258; Collection<Feature> fs = SHPUtil.loadSHP(basePath+"test/testQ.shp", epsg).fs;
		for(Feature f : fs) f.id = ""+f.getProperties().get("NUTS_P_ID");

		LOGGER.info("Dissolve by id");
		dissolveById(fs);

		LOGGER.info("Make valid");
		fs = makeMultiPolygonValid(fs);

		LOGGER.info("Clip");
		double eps = 1e-9;
		clip(fs, new Envelope(-180+eps, 180-eps, -90+eps, 90-eps));

		LOGGER.info("Ensure tesselation and fix noding");
		final double nodingResolution = 1e-7;
		fs = Partition.runRecursively(fs, new Operation() {
			public void run(Partition p) {
				LOGGER.info(p);
				ensureTesselation_(p.getFeatures());
				NodingUtil.fixNoding(NodingIssueType.PointPoint, p.getFeatures(), nodingResolution);
				NodingUtil.fixNoding(NodingIssueType.LinePoint, p.getFeatures(), nodingResolution);
			}}, 3000000, 15000, false);

		LOGGER.info("Save");
		for(Feature f : fs) f.setGeom(JTSGeomUtil.toMulti(f.getGeom()));
		SHPUtil.saveSHP(fs, basePath+"nutsplus/", "NUTS_PLUS_01M_1403_clean.shp");
		//SHPUtil.saveSHP(fs, basePath+"test/", "testQ_clean.shp");

		System.out.println("End");
	}






	public static void dissolveById(Collection<Feature> fs) {
		//index features by id
		HashMap<String,List<Feature>> ind = new HashMap<String,List<Feature>>();
		for(Feature f : fs) {
			List<Feature> col = ind.get(f.id);
			if(col == null) {
				col = new ArrayList<Feature>();
				ind.put(f.id, col);
			}
			col.add(f);
		}

		//merge features having same id
		for(List<Feature> col : ind.values()) {
			if(col.size() == 1) continue;
			Collection<MultiPolygon> polys = new ArrayList<MultiPolygon>();
			for(Feature f : col) polys.add((MultiPolygon) f.getGeom());
			MultiPolygon mp = (MultiPolygon) JTSGeomUtil.toMulti(CascadedPolygonUnion.union(polys));
			for(int i=1; i<col.size(); i++) fs.remove(col.get(i));
			col.get(0).setGeom(mp);
		}
	}




	public static Collection<Feature> makeMultiPolygonValid(Collection<Feature> fs) {
		for(Feature f : fs)
			f.setGeom( (MultiPolygon)JTSGeomUtil.toMulti(f.getGeom().buffer(0)) );
		return fs;
	}
	public static void makeMultiPolygonValid(String inputFile, String outputPath, String outputFile) {
		SHPUtil.saveSHP(makeMultiPolygonValid(SHPUtil.loadSHP(inputFile).fs), outputPath, outputFile);
	}





	public void ensureTesselation(String inputFile, String outputPath, String outputFile, boolean ensureMultiPolygon) {
		Collection<Feature> fs = SHPUtil.loadSHP(inputFile).fs;
		fs = ensureTesselation(fs);
		if(ensureMultiPolygon) for(Feature f : fs) f.setGeom((MultiPolygon)JTSGeomUtil.toMulti(f.getGeom()));
		SHPUtil.saveSHP(fs, outputPath, outputFile);
	}

	public static Collection<Feature> ensureTesselation(Collection<Feature> units) {
		Collection<Feature> out = Partition.runRecursively(units, new Operation() {
			public void run(Partition p) {
				LOGGER.info(p);
				ensureTesselation_(p.getFeatures());
			}}, 3000000, 15000, false);
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
					ui.setGeom((MultiPolygon)JTSGeomUtil.toMulti(g2));
					index.insert(ui.getGeom().getEnvelopeInternal(), ui);
				}
			}
		}

	}


	public static Collection<Feature> fixNoding(Collection<Feature> units, final double nodingResolution) {
		Collection<Feature> out = Partition.runRecursively(units, new Operation() {
			public void run(Partition p) {
				LOGGER.info(p);
				NodingUtil.fixNoding(NodingIssueType.PointPoint, p.getFeatures(), nodingResolution);
				NodingUtil.fixNoding(NodingIssueType.LinePoint, p.getFeatures(), nodingResolution);
			}}, 3000000, 15000, false);
		return out;
	}



	public static void clip(Collection<Feature> fs, Envelope env) {
		Polygon extend = JTS.toGeometry(env);
		Collection<Feature> toBeRemoved = new HashSet<Feature>();

		for(Feature f : fs) {
			Geometry g = f.getGeom();
			Envelope env_ = g.getEnvelopeInternal();

			//feature fully in the envelope
			if(env.contains(env_)) continue;
			//feature fully out of the envelope
			if(!env.intersects(env_)) {
				toBeRemoved.add(f);
				continue;
			}

			//check if feature intersects envelope
			Geometry inter = g.intersection(extend);
			inter = JTSGeomUtil.keepOnlyPolygonal(inter);
			if(inter.isEmpty()) {
				toBeRemoved.add(f);
				continue;
			}

			//update geometry with intersection
			f.setGeom((MultiPolygon)inter);
		}

		fs.removeAll(toBeRemoved);
	}

}