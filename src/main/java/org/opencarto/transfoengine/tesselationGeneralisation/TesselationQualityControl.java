/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.apache.log4j.Logger;
import org.opencarto.transfoengine.Engine;

import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.quadtree.Quadtree;

/**
 * 
 * Default procedure for basic generalisation of statistical units tesselations.
 * 
 * @author julien Gaffuri
 *
 */
public class TesselationQualityControl {
	private final static Logger LOGGER = Logger.getLogger(TesselationQualityControl.class);

	public static void run(ATesselation t, String logFileFolder){

		//build spatial index for units
		SpatialIndex index = new Quadtree();
		for(AUnit a : t.aUnits) index.insert(a.getObject().getGeom().getEnvelopeInternal(), a.getObject());

		LOGGER.info("   Set units constraints");
		for(AUnit a : t.aUnits)
			a.addConstraint(new CUnitDoNotOverlap(a, index));

		LOGGER.info("   Activate units");
		Engine<AUnit> uEng = new Engine<AUnit>(t.aUnits, logFileFolder+"/units.log");
		uEng.getLogWriter().println("******** Activate units ********");
		uEng.shuffle();  uEng.activateQueue();
		uEng.closeLogger();
	}

}
