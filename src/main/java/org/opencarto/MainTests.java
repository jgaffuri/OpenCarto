package org.opencarto;

import java.util.Collection;
import java.util.HashMap;

import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.util.FeatureUtil;

public class MainTests {

	public static void main(String[] args) {
		System.out.println("Start");

		String basePath = "/home/juju/Bureau/nuts_gene_data/";

		System.out.println("Load data");
		//Collection<Feature> fs = SHPUtil.loadSHP(basePath+"commplus/COMM_PLUS_clean.shp").fs;
		Collection<Feature> fs = SHPUtil.loadSHP(basePath+"out/100k_1M/commplus/COMM_PLUS_WM_1M_1.shp").fs;

		System.out.println(fs.size());
		System.out.println(FeatureUtil.getNumberVertices(fs));

		System.out.println("Compute id check");
		HashMap<String, Integer> cnts = FeatureUtil.checkIdentfier(fs, "GISCO_ID");
		System.out.println(cnts);

		System.out.println("End");
	}	

}
