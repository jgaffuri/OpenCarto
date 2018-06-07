package org.opencarto.gisco;

import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.opencarto.algo.matching.LevenshteinMatching;
import org.opencarto.algo.matching.LevenshteinMatching.Match;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.CSVUtil;
import org.opencarto.io.SHPUtil;
import org.opencarto.util.FeatureUtil;

public class MapNiger {
	private final static Logger LOGGER = Logger.getLogger(MapNiger.class.getName());

	public static void main(String[] args) {
		System.out.println("Start");

		String basePath_ = "/home/juju/Bureau/niger/";
		String basePath = basePath_+"data/";


		/*LOGGER.info("Load commune data");
		String inFile = basePath+"commune_niger.shp";
		Collection<Feature> units = SHPUtil.loadSHP(inFile).fs;
		for(Feature f : units) f.id = ""+f.get("CODECOMMUN");*/

		LOGGER.info("Load location data");
		//Collection<Feature> locs = SHPUtil.loadSHP(basePath+"renacom.shp").fs;
		//for(Feature f : locs) f.id = ""+f.get("CODE_LOCAL");
		Collection<Feature> locs = SHPUtil.loadSHP(basePath+"commune_niger.shp").fs;
		for(Feature f : locs) f.id = ""+f.get("CODECOMMUN");

		LOGGER.info("Load project data");
		Collection<Feature> projects = FeatureUtil.toFeatures( CSVUtil.load(basePath_+"base_donnee.csv") );

		//overrides
		HashMap<String, String> overrides = new HashMap<String, String>();
		overrides.put("Zinder Arrondissement communal I", "ZINDER ARR. 1");
		overrides.put("Zinder Arrondissement communal II", "ZINDER ARR. 2");
		overrides.put("Zinder Arrondissement communal III", "ZINDER ARR. 3");
		overrides.put("Zinder Arrondissement communal IV", "ZINDER ARR. 4");
		overrides.put("Zinder Arrondissement communal V", "ZINDER ARR. 5");
		overrides.put("Kourni Koutchika", "KOURNI");
		overrides.put("Kangna Wamé", "WAME");
		overrides.put("Tahoua 1", "TAHOUA ARR. 1");
		overrides.put("Tahoua 2", "TAHOUA ARR. 2");
		overrides.put("Niamey", "NIAMEY ARR. 1");
		overrides.put("Belbedji", "TARKA");
		overrides.put("Damagaram ", "Damagaram takaya");
		
		//overrides.put("Aderbissinat", "ADARBISNAT");
		/*overrides.put("Zinder Arrondissement communal I", "ZINDER I");
		overrides.put("Zinder Arrondissement communal II", "ZINDER II");
		overrides.put("Zinder Arrondissement communal III", "ZINDER III");
		overrides.put("Zinder Arrondissement communal IV", "ZINDER IV");
		overrides.put("Zinder Arrondissement communal V", "ZINDER V");*/


		LOGGER.info("Compute matching + join geometries");
		Collection<Match> ms = LevenshteinMatching.joinGeometry(projects, "commune", locs, "COMMUNE", overrides, true);

		int sum=0;
		for(Match m : ms) sum += m.cost;
		System.out.println(sum);

		LOGGER.info("Save matching");
		LevenshteinMatching.saveAsCSV(ms,"/home/juju/Bureau/niger/matching.csv");

		LOGGER.info("Save output");
		SHPUtil.saveSHP(projects, basePath_+"projects.shp", SHPUtil.getCRS(basePath+"renacom.shp"));



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


}
