package org.opencarto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opencarto.datamodel.Feature;
import org.opencarto.io.CSVUtil;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.CartographicResolution;
import org.opencarto.util.FeatureUtil;

public class MainTests {

	public static void main(String[] args) {
		System.out.println("Start");

		String basePath = "/home/juju/Bureau/nuts_gene_data/";

		for(int i=1; i<=6; i++) {

			System.out.println("Load data "+i);
			//Collection<Feature> fs = SHPUtil.loadSHP(basePath+"commplus/COMM_PLUS_clean.shp").fs;
			Collection<Feature> fs = SHPUtil.loadSHP(basePath+"out/100k_1M/commplus/COMM_PLUS_WM_1M_"+i+".shp").fs;
			for(Feature f : fs) for(String id : new String[] {"NUTS_ID","COMM_ID","idgene","GISCO_ID"}) if(f.getProperties().get(id) != null) f.id = ""+f.getProperties().get(id);

			System.out.println(fs.size());
			System.out.println(FeatureUtil.getNumberVertices(fs));

			System.out.println("Compute id check");
			HashMap<String, Integer> cnts = FeatureUtil.checkIdentfier(fs, "GISCO_ID");
			System.out.println(cnts);

			System.out.println("Compute polygons area analysis");
			final CartographicResolution res = new CartographicResolution(1e6);
			ArrayList<Map<String, Object>> data = FeatureUtil.getInfoSmallPolygons(fs, res.getPerceptionSizeSqMeter());
			CSVUtil.save(data, "/home/juju/Bureau/", "area_analysis_"+i+".csv");

		}
		System.out.println("End");
	}	

}
