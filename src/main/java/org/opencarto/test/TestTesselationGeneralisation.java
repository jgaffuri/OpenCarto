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
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeFaceSize;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeGranularity;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeNoTriangle;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeValidity;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgesFacesContainPoints;
import org.opencarto.transfoengine.tesselationGeneralisation.CFaceContainPoints;
import org.opencarto.transfoengine.tesselationGeneralisation.CFaceSize;
import org.opencarto.transfoengine.tesselationGeneralisation.CFaceNoTriangle;
import org.opencarto.transfoengine.tesselationGeneralisation.CFaceValidity;
import org.opencarto.transfoengine.tesselationGeneralisation.CUnitContainPoints;
import org.opencarto.transfoengine.tesselationGeneralisation.CUnitNoNarrowGaps;
import org.opencarto.transfoengine.tesselationGeneralisation.CUnitNoNarrowParts;
import org.opencarto.transfoengine.tesselationGeneralisation.CUnitNoTriangle;
import org.opencarto.transfoengine.tesselationGeneralisation.TesselationGeneralisation;
import org.opencarto.transfoengine.tesselationGeneralisation.TesselationGeneralisationSpecifications;

import com.vividsolutions.jts.geom.Point;

/**
 * @author julien Gaffuri
 *
 */
public class TestTesselationGeneralisation {
	private final static Logger LOGGER = Logger.getLogger(TestTesselationGeneralisation.class.getName());

	//TODO include noding as a constraint at tesselation level
	//TODO implement narrow corridor removal
	//TODO narrow gaps/parts: make a single? compute geometry. check effect on neigbours/points
	//TODO test with large scale changes - fix issues
	//TODO deployment
	//TODO removal of large elongated faces/holes: face size constraint: take into account shape - use erosion? use width evaluation method?
	//TODO face collapse algorithm
	//TODO edge size constraint: fix it!
	//TODO archipelagos detection

	public static void main(String[] args) {
		LOGGER.info("Start");

		LOGGER.info("Load data");
		Collection<Feature> units = SHPUtil.loadSHP("src/test/resources/testTesselationGeneralisation.shp", 3035).fs;
		for(Feature unit : units) unit.id = unit.getProperties().get("id").toString();
		HashMap<String, Collection<Point>> points = TesselationGeneralisation.loadPoints("src/test/resources/testTesselationGeneralisationPoints.shp", "id");

		LOGGER.info("Launch generalisation");
		double scaleDenominator = 1e6; int roundNb = 10;
		units = TesselationGeneralisation.runGeneralisation(units, points, specifications, scaleDenominator, roundNb, false, 1000000, 1000);

		LOGGER.info("Save output data");
		SHPUtil.saveSHP(units, "target/", "testTesselationGeneralisation_out.shp");

		LOGGER.info("End");
	}


	public static TesselationGeneralisationSpecifications specifications = new TesselationGeneralisationSpecifications() {
		boolean preserveAllUnits = true;
		boolean preserveIfPointsInIt = true;
		boolean noTriangle = true;
		double nodingResolution = 1e-5;
		int quad = 4;

		public void setTesselationConstraints(ATesselation t, CartographicResolution res) {}
		public void setUnitConstraints(ATesselation t, CartographicResolution res) {
			for(AUnit a : t.aUnits) {
				a.addConstraint(new CUnitNoNarrowGaps(a, res.getSeparationDistanceMeter(), nodingResolution, quad, preserveAllUnits, preserveIfPointsInIt).setPriority(10));
				a.addConstraint(new CUnitNoNarrowParts(a, res.getSeparationDistanceMeter(), nodingResolution, quad, preserveAllUnits, preserveIfPointsInIt).setPriority(9));
				if(preserveIfPointsInIt) a.addConstraint(new CUnitContainPoints(a));
				if(noTriangle) a.addConstraint(new CUnitNoTriangle(a));
			}
		}
		public void setTopologicalConstraints(ATesselation t, CartographicResolution res) {
			for(AFace a : t.aFaces) {
				a.addConstraint(new CFaceSize(a, 0.1*res.getPerceptionSizeSqMeter(), 3*res.getPerceptionSizeSqMeter(), res.getPerceptionSizeSqMeter(), preserveAllUnits, preserveIfPointsInIt).setPriority(2));
				a.addConstraint(new CFaceValidity(a));
				if(preserveIfPointsInIt) a.addConstraint(new CFaceContainPoints(a));
				if(noTriangle) a.addConstraint(new CFaceNoTriangle(a));
			}
			for(AEdge a : t.aEdges) {
				a.addConstraint(new CEdgeGranularity(a, 2*res.getResolutionM()));
				a.addConstraint(new CEdgeValidity(a));
				if(noTriangle) a.addConstraint(new CEdgeNoTriangle(a));
				a.addConstraint(new CEdgeFaceSize(a).setImportance(6));
				if(preserveIfPointsInIt) a.addConstraint(new CEdgesFacesContainPoints(a));
			}
		}
	};

}
