/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.algo.polygon.MorphologicalAnalysis;
import org.opencarto.transfoengine.ConstraintOneShot;
import org.opencarto.transfoengine.Transformation;

/**
 * @author julien Gaffuri
 *
 */
public class CTesselationMorphology extends ConstraintOneShot<ATesselation> {

	public CTesselationMorphology(ATesselation agent, Transformation<ATesselation> transformation) {
		super(agent, transformation);
		
		//TODO move somewhere else - constraint at tesselation level ?
		MorphologicalAnalysis.removeNarrowGapsTesselation(t.getUnits(), res.getSeparationDistanceMeter(), 5, 1e-5);

	}

}
