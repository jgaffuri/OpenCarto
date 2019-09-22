package org.opencarto.test;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.tesselationGeneralisation.TesselationQuality;

/**
 * @author julien Gaffuri
 *
 */
public class TestQualityCheck {
	private final static Logger LOGGER = Logger.getLogger(TestQualityCheck.class.getName());

	public static void main(String[] args) {
		System.out.println("Start");

		LOGGER.info("Load data");
		Collection<Feature> units = SHPUtil.loadSHP("src/test/resources/testTesselationGeneralisation.shp").fs;
		for(Feature unit : units) unit.setID( unit.get("id").toString() );

		LOGGER.info("Run quality check");
		final double nodingResolution = 1e-7;
		TesselationQuality.checkQuality(units, nodingResolution, "target/eval_units.csv", true, 3000000, 15000, true);

		/*
		LOGGER.info("Check identifier");
		HashMap<String, Integer> ids = FeatureUtil.checkIdentfier(units, "id");
		System.out.println(ids);
		 */

		System.out.println("End");
	}

}
