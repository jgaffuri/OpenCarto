package org.opencarto.gisco;

import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.opencarto.algo.matching.MatchingUtil;
import org.opencarto.algo.matching.MatchingUtil.Match;
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


		LOGGER.info("Load commune data");
		String inFile = basePath+"commune_niger.shp";
		Collection<Feature> units = SHPUtil.loadSHP(inFile).fs;
		for(Feature f : units) f.id = ""+f.get("CODECOMMUN");

		LOGGER.info("Load project data");
		Collection<Feature> projects = FeatureUtil.toFeatures( CSVUtil.load(basePath_+"base_donnee.csv") );

		LOGGER.info("Compute matching");
		Collection<Match> ms = MatchingUtil.getMatchingMinLevenshteinDistance(projects,"Commune", units,"COMMUNE", true, true, true, true);
		HashMap<String,Match> msI = MatchingUtil.index(ms);
		ms = null;

		LOGGER.info("Override matching");
		MatchingUtil.override(msI, "Zinder Arrondissement communal I", "ZINDER ARR. 1");
		MatchingUtil.override(msI, "Zinder Arrondissement communal II", "ZINDER ARR. 2");
		MatchingUtil.override(msI, "Zinder Arrondissement communal III", "ZINDER ARR. 3");
		MatchingUtil.override(msI, "Zinder Arrondissement communal IV", "ZINDER ARR. 4");
		MatchingUtil.override(msI, "Zinder Arrondissement communal V", "ZINDER ARR. 5");
		MatchingUtil.override(msI, "Kourni Koutchika", "KOURNI");
		MatchingUtil.override(msI, "Takaya", "TAMAYA");
		MatchingUtil.override(msI, "Maine Sora", "MAINE SOROA");
		MatchingUtil.override(msI, "Matamèye ", "MATAMEY");
		MatchingUtil.override(msI, "Tchintabaraben", "TCHINTABARADEN");
		MatchingUtil.override(msI, "Dan Tchiao", "DANTCHIAO");
		MatchingUtil.override(msI, "Bambey", "BAMBEYE");
		MatchingUtil.override(msI, "Gafati", "GAFFATI");
		MatchingUtil.override(msI, "N’Guigmi", "N'GUIGMI");
		MatchingUtil.override(msI, "Kangna Wamé", "MIRRIAH");
		MatchingUtil.override(msI, "Damagaram ", "DAMAGARAM TAKAYA");
		MatchingUtil.override(msI, "Tahoua 1", "TAHOUA ARR. 1");
		MatchingUtil.override(msI, "Tahoua 2", "TAHOUA ARR. 2");

		int sum=0;
		for(Match m : msI.values()) {
			//System.out.println(m.cost + "," + m.s1 + "," + m.s2);
			sum += m.cost;
		}
		System.out.println(sum);

		LOGGER.info("Save");
		MatchingUtil.save(msI.values(),"/home/juju/Bureau/matching.csv");


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
