package org.opencarto.gisco;

import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.util.FeatureUtil;

public class MainCheckIdentifier {
	private final static Logger LOGGER = Logger.getLogger(MainCheckIdentifier.class.getName());

	public static void main(String[] args) {
		LOGGER.info("Start");

		LOGGER.info("Load data");
		Collection<Feature> units = SHPUtil.loadSHP("/home/juju/Bureau/nuts_gene_data/nutsplus/NUTS_PLUS_01M_1904.shp").fs;
		LOGGER.info("Check id");
		HashMap<String, Integer> ids = FeatureUtil.checkIdentfier(units, "NUTS_P_ID");
		System.out.println(ids);

		LOGGER.info("End");
	}

}
