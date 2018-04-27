package org.opencarto.gisco;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.CSVUtil;
import org.opencarto.io.SHPUtil;

public class MapNiger {
	private final static Logger LOGGER = Logger.getLogger(MapNiger.class.getName());

	public static void main(String[] args) {
		System.out.println("Start");

		String basePath_ = "/home/juju/Bureau/niger/";
		String basePath = basePath_+"data/";


		LOGGER.info("Load data");
		String inFile = basePath+"commune_niger.shp";
		Collection<Feature> units = SHPUtil.loadSHP(inFile).fs;
		for(Feature f : units) f.id = ""+f.getProperties().get("CODECOMMUN");

		//do the join stuff
		//make it over several columns
		//input: 2 CSV files. Each with id. output: matching from id2 to id1

		ArrayList<HashMap<String, String>> ps = CSVUtil.load(basePath_+"base_donnee.csv");
		int id=1; for(HashMap<String, String> p : ps) p.put("id", "p"+(id++));
		System.out.println(ps);
		//transform into feature collection
		Collection<Feature> projects = null;

		Collection<Mapping> map = getMapping(units, projects);
		//extract best mappings for projects
		//export as a csv, with wkt?
		//export as SHP with commune areas? center points?


		//LOGGER.info("Fix quality");
		//double eps = 1e-9;
		//units = TesselationQuality.fixQuality(units, new Envelope(-180+eps, 180-eps, -90+eps, 90-eps), 1e-7, 3000000, 15000);

		//SHPUtil.saveSHP(FeatureUtil.dissolve(units, "CODEDEPART"), basePath+"dept_niger.shp", SHPUtil.getCRS(inFile));
		//SHPUtil.saveSHP(FeatureUtil.dissolve(units, "CODEREGION"), basePath+"region_niger.shp", SHPUtil.getCRS(inFile));

		/*
		LOGGER.info("Save");
		//for(Feature f : units) f.setGeom(JTSGeomUtil.toMulti(f.getGeom()));
		SHPUtil.saveSHP(units, basePath+"commune_niger_fix.shp", SHPUtil.getCRS(inFile));
		//SHPUtil.saveSHP(fs, basePath+"test/", "testQ_clean.shp", SHPUtil.getCRS(inFile));
		 */

		System.out.println("End");
	}


	class Mapping { String id1, id2; double prob = 0; }
	private static Collection<Mapping> getMapping(Collection<Feature> units, Collection<Feature> features) {
		return null;
	}



}
