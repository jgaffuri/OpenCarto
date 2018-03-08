package org.opencarto;

import java.util.ArrayList;
import java.util.Collection;
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

		System.out.println("Load data");
		//Collection<Feature> fs = SHPUtil.loadSHP(basePath+"commplus/COMM_PLUS_clean.shp").fs;
		Collection<Feature> fs = SHPUtil.loadSHP(basePath+"out/100k_1M/commplus/COMM_PLUS_WM_1M_1.shp").fs;
		for(Feature f : fs) for(String id : new String[] {"NUTS_ID","COMM_ID","idgene","GISCO_ID"}) if(f.getProperties().get(id) != null) f.id = ""+f.getProperties().get(id);

		/*
		System.out.println(fs.size());
		System.out.println(FeatureUtil.getNumberVertices(fs));

		System.out.println("Compute id check");
		HashMap<String, Integer> cnts = FeatureUtil.checkIdentfier(fs, "GISCO_ID");
		System.out.println(cnts);
		 */

		final CartographicResolution res = new CartographicResolution(1e6);
		ArrayList<Map<String, Object>> data = FeatureUtil.analysePolygonsSizes(fs, res.getPerceptionSizeSqMeter());
		CSVUtil.save(data, "/home/juju/Bureau/", "area_analysis.csv");

		System.out.println("End");
	}	

}
