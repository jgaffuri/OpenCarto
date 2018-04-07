/**
 * 
 */
package org.opencarto.test;

import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.tesselationGeneralisation.TesselationGeneralisation;

import com.vividsolutions.jts.geom.Point;

/**
 * @author julien Gaffuri
 *
 */
public class TestTesselationGeneralisation {
	private final static Logger LOGGER = Logger.getLogger(TestTesselationGeneralisation.class.getName());

	//TODO deployment
	//TODO include noding as a constraint at tesselation level ???
	//TODO implement/fix narrow corridor removal
	//TODO narrow gaps/parts: make a single? compute geometry. check effect on neigbours/points
	//TODO handle geographical coordinates
	//TODO test with large scale changes - fix issues
	//TODO removal of large elongated faces/holes: face size constraint: take into account shape - use erosion? use width evaluation method?
	//TODO face collapse algorithm - for small and compact faces only
	//TODO edge size constraint: fix it!

	public static void main(String[] args) {
		LOGGER.info("Start");

		LOGGER.info("Load data");
		Collection<Feature> units = SHPUtil.loadSHP("src/test/resources/testTesselationGeneralisation.shp", 3035).fs;
		for(Feature unit : units) unit.id = unit.getProperties().get("id").toString();
		HashMap<String, Collection<Point>> points = TesselationGeneralisation.loadPoints("src/test/resources/testTesselationGeneralisationPoints.shp", "id");

		LOGGER.info("Launch generalisation");
		double scaleDenominator = 1e6; int roundNb = 10;
		units = TesselationGeneralisation.runGeneralisation(units, points, TesselationGeneralisation.defaultSpecs, scaleDenominator, roundNb, false, 1000000, 1000);

		LOGGER.info("Save output data");
		SHPUtil.saveSHP(units, "target/testTesselationGeneralisation_out.shp");

		LOGGER.info("End");
	}

}
