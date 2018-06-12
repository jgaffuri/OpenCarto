package org.opencarto.gisco;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


		LOGGER.info("Load project data");
		Collection<Feature> ps = FeatureUtil.toFeatures( CSVUtil.load(basePath_+"base_donnee.csv") );

		List<String> secteurs = FeatureUtil.getPropValuesAsList(ps, "secteur");
		LOGGER.info(secteurs.size() + " unique secteurs found");
		List<String> partenas = FeatureUtil.getPropValuesAsList(ps, "partena");
		LOGGER.info(partenas.size() + " unique partenaires found");

		LOGGER.info("Aggregate project data at commune level");
		HashMap<String, Map<String, Object>> cs = new HashMap<String, Map<String, Object>>();
		//for each commune, compute the sum of amount, number of projects + breakdown by partner and sector
		for(Feature p : ps) {
			//get commune
			String key = p.get("commune").toString();
			Map<String, Object> c = cs.get(key);
			if(c == null) {
				//create new
				c = new HashMap<String, Object>();
				c.put("commune", p.get("commune"));
				c.put("dep", p.get("dep"));
				c.put("region", p.get("region"));
				c.put("nb","0");
				c.put("montant","0");
				for(int i=0; i<secteurs.size(); i++) {
					c.put("nb_s_"+i,"0");
					c.put("m_s_"+i,"0");
				}
				for(int i=0; i<partenas.size(); i++) {
					c.put("nb_p_"+i,"0");
					c.put("m_p_"+i,"0");
				}
				cs.put(key, c);
			}
			//add data
			c.put("nb", ""+(Integer.parseInt(c.get("nb").toString())+1));
			int montant = Integer.parseInt(p.get("montant").toString());
			c.put("montant", "" + (Integer.parseInt(c.get("montant").toString()) + montant));

			int i_s = secteurs.indexOf( p.get("secteur").toString() );
			c.put("nb_s_"+i_s, ""+(Integer.parseInt(c.get("nb_s_"+i_s).toString())+1));
			c.put("m_s_"+i_s, ""+(Integer.parseInt(c.get("m_s_"+i_s).toString())+montant));

			int i_p = partenas.indexOf( p.get("partena").toString() );
			c.put("nb_p_"+i_p, ""+(Integer.parseInt(c.get("nb_p_"+i_p).toString())+1));
			c.put("m_p_"+i_p, ""+(Integer.parseInt(c.get("m_p_"+i_p).toString())+montant));
		}
		ps.clear(); ps=null;

		LOGGER.info("Save");
		CSVUtil.save(cs.values(), basePath_+"projets_par_commune.csv");

		LOGGER.info("Get project data");
		Collection<Feature> projectsByComm = FeatureUtil.toFeatures(cs.values());
		cs.clear(); cs = null;

		LOGGER.info("Load commune data");
		Collection<Feature> locs = SHPUtil.loadSHP(basePath+"commune_niger.shp").fs;
		for(Feature f : locs) f.id = ""+f.get("CODECOMMUN");
		//Collection<Feature> locs = SHPUtil.loadSHP(basePath+"renacom.shp").fs;
		//for(Feature f : locs) f.id = ""+f.get("CODE_LOCAL");


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
		overrides.put("Damagaram ", "DAMAGARAM TAKAYA");
		overrides.put("Garazou", "ALAKOSS");
		overrides.put("Gangara", "GANGARA (AGUIE)");

		LOGGER.info("Compute matching + join geometries");
		Collection<Match> ms = LevenshteinMatching.joinGeometry(projectsByComm, "commune", locs, "COMMUNE", overrides, true);

		int sum=0;
		for(Match m : ms) sum += m.cost;
		System.out.println(sum);

		LOGGER.info("Save matching");
		LevenshteinMatching.saveAsCSV(ms,"/home/juju/Bureau/niger/matching.csv");

		LOGGER.info("Save output");
		for(Feature p : projectsByComm) p.setGeom(p.getGeom().getCentroid());
		SHPUtil.saveSHP(projectsByComm, basePath_+"commune_projects.shp", SHPUtil.getCRS(basePath+"commune_niger.shp"));


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
