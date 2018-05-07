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


		LOGGER.info("Load data");
		String inFile = basePath+"commune_niger.shp";
		Collection<Feature> units = SHPUtil.loadSHP(inFile).fs;
		for(Feature f : units) f.id = ""+f.get("CODECOMMUN");

		Collection<Feature> projects = FeatureUtil.toFeatures( CSVUtil.load(basePath_+"base_donnee.csv") );

		//compute mappings
		Collection<Mapping> ms = getMapping(units, projects);
		for(Mapping map : ms) {
			Feature f = map.f, u = map.unit;
			System.out.println(map.cost + "," + f.get("Region") + "," + u.get("REGION") + "," + f.get("departement") + "," + u.get("DEPARTEMEN") + "," + f.get("Commune") + "," + u.get("COMMUNE"));
		}
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


	static class Mapping { Feature f, unit; double cost = 0; }
	private static Collection<Mapping> getMapping(Collection<Feature> units, Collection<Feature> features) {
		Collection<Mapping> out = new ArrayList<Mapping>();
		for(Feature f : features) {
			//evaluate distance to each unit
			Mapping map = new Mapping(); map.cost = Integer.MAX_VALUE; map.f = f;
			for(Feature u : units) {
				//evaluate distance f/u
				int d = 0;
				String s1 = f.get("Commune") + "____" + f.get("departement") + "____" + f.get("Region");
				String s2 = u.get("COMMUNE") + "____" + u.get("DEPARTEMEN") + "____" + u.get("REGION");
				d += Util.getLevenshteinDistance(s1,s2, true, true, true, true);
				//d += Util.getLevenshteinDistance(f.get("Commune").toString(), u.get("COMMUNE").toString(), true, true, true, true);
				//d += Util.getLevenshteinDistance(f.get("departement").toString(), u.get("DEPARTEMEN").toString(), true, true, true, true);
				//d += Util.getLevenshteinDistance(f.get("Region").toString(), u.get("REGION").toString(), true, true, true, true);
				if(d>map.cost) continue;
				map.cost = d; map.unit = u;
			}
			out.add(map);
		}
		return out;
	}

}
