/**
 * 
 */
package org.opencarto.test;

import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.CartographicResolution;
import org.opencarto.transfoengine.tesselationGeneralisation.AEdge;
import org.opencarto.transfoengine.tesselationGeneralisation.AFace;
import org.opencarto.transfoengine.tesselationGeneralisation.ATesselation;
import org.opencarto.transfoengine.tesselationGeneralisation.AUnit;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeGranularity;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeValidity;
import org.opencarto.transfoengine.tesselationGeneralisation.DefaultTesselationGeneralisation;
import org.opencarto.transfoengine.tesselationGeneralisation.TesselationGeneralisationSpecifications;

import com.vividsolutions.jts.geom.Point;

/**
 * @author julien Gaffuri
 *
 */
public class TestTesselationGeneralisation {
	private final static Logger LOGGER = Logger.getLogger(TestTesselationGeneralisation.class.getName());

	public static void main(String[] args) {
		LOGGER.info("Start");

		LOGGER.info("Load data");
		Collection<Feature> units = SHPUtil.loadSHP("src/test/resources/testTesselationGeneralisation.shp", 3035).fs;
		for(Feature unit : units) unit.id = unit.getProperties().get("id").toString();
		//HashMap<String, Collection<Point>> ptData = null;
		HashMap<String, Collection<Point>> points = DefaultTesselationGeneralisation.loadPoints("src/test/resources/testTesselationGeneralisationPoints.shp", "id");

		LOGGER.info("Launch generalisation");
		double scaleDenominator = 1e6; int roundNb = 1;
		units = DefaultTesselationGeneralisation.runGeneralisation(units, points, specifications, scaleDenominator, roundNb, false, 1000000, 1000);

		LOGGER.info("Save output data");
		SHPUtil.saveSHP(units, "target/", "testTesselationGeneralisation_out.shp");

		LOGGER.info("End");
	}


	public static TesselationGeneralisationSpecifications specifications = new TesselationGeneralisationSpecifications() {
		public void setTesselationConstraints(ATesselation t, CartographicResolution res) {}
		public void setUnitConstraints(ATesselation t, CartographicResolution res) {
			for(AUnit a : t.aUnits) {
				//a.addConstraint(new CUnitNoNarrowGaps(a, res.getSeparationDistanceMeter(), 1e-5, 5, true).setPriority(10));
				//a.addConstraint(new CUnitContainPoints(a));
			}
		}
		public void setTopologicalConstraints(ATesselation t, CartographicResolution res) {
			for(AFace a : t.aFaces) {
				//a.addConstraint(new CFaceSize(a, 0.1*res.getPerceptionSizeSqMeter(), 3*res.getPerceptionSizeSqMeter(), res.getPerceptionSizeSqMeter(), true, true).setPriority(2));
				//a.addConstraint(new CFaceValidity(a));
				//a.addConstraint(new CFaceContainPoints(a));
			}
			for(AEdge a : t.aEdges) {
				a.addConstraint(new CEdgeGranularity(a, 2*res.getResolutionM(), true));
				a.addConstraint(new CEdgeValidity(a));
				//a.addConstraint(new CEdgeFaceSize(a).setImportance(6));
				//a.addConstraint(new CEdgeTriangle(a));
				//a.addConstraint(new CEdgesFacesContainPoints(a));
			}
		}
	};

}
