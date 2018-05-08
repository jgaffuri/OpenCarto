package org.opencarto.gisco;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.CSVUtil;
import org.opencarto.io.SHPUtil;
import org.opencarto.util.FeatureUtil;
import org.opencarto.util.Util;

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

		LOGGER.info("Build mapping properties");
		for(Feature p : projects) p.set("map", p.get("Commune") + "____" + p.get("departement") + "____" + p.get("Region"));
		for(Feature u : units) u.set("map", u.get("COMMUNE") + "____" + u.get("DEPARTEMEN") + "____" + u.get("REGION"));

		LOGGER.info("Apply override");
		//TODO

		LOGGER.info("Compute mappings");
		Collection<Mapping> ms = getMappingMinLevenshteinDistance(projects, "map", units, "map", true, true, true, true);
		for(Mapping map : ms) {
			Feature f = map.f1, u = map.f2;
			System.out.println(map.cost + "," + f.get("map") + "," + u.get("map"));
		}
		//do corrections - override
		//export mapping result
		//use result


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


	static class Mapping { Feature f1, f2; double cost = 0; }
	public static Collection<Mapping> getMappingMinLevenshteinDistance(Collection<Feature> f1s, String propF1, Collection<Feature> f2s, String propF2, boolean toLowerCase, boolean trim, boolean stripDiacritics, boolean stripWeirdCaracters) {
		Collection<Mapping> out = new ArrayList<Mapping>();
		for(Feature f1 : f1s) {
			//evaluate distance to each f2, keeping the minimum one
			Mapping map = new Mapping(); map.cost = Integer.MAX_VALUE; map.f1 = f1;
			for(Feature f2 : f2s) {
				//evaluate distance f/u
				int d = Util.getLevenshteinDistance(f1.get(propF1).toString(),f2.get(propF2).toString(), toLowerCase, trim, stripDiacritics, stripWeirdCaracters);
				if(d>map.cost) continue;
				map.cost = d; map.f2 = f2;
			}
			out.add(map);
		}
		return out;
	}

}
