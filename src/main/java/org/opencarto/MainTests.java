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
		int epsg = 4258; Collection<Feature> fs = SHPUtil.loadSHP(basePath+"commplus/COMM_PLUS_clean.shp", epsg).fs;
		System.out.println("Compute id check");
		HashMap<String, Integer> cnts = FeatureUtil.checkIdentfier(fs, "GISCO_ID");
		System.out.println(cnts);

		System.out.println("End");
	}	

}
