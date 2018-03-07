package org.opencarto;

import java.util.Collection;

import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.util.FeatureUtil;

public class MainTests {

	public static void main(String[] args) {

		String basePath = "/home/juju/Bureau/nuts_gene_data/";
		int epsg = 4258; Collection<Feature> fs = SHPUtil.loadSHP(basePath+"commplus/COMM_PLUS.shp", epsg).fs;

		cnts = FeatureUtil.
	}	

}
