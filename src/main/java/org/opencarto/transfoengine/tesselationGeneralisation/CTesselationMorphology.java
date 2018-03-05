/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.algo.polygon.MorphologicalAnalysis;
import org.opencarto.transfoengine.ConstraintOneShot;
import org.opencarto.transfoengine.TransformationNonCancellable;

/**
 * @author julien Gaffuri
 *
 */
public class CTesselationMorphology extends ConstraintOneShot<ATesselation> {

	public CTesselationMorphology(ATesselation agent, final double separationDistance, final double nodingDistance) {
		super(agent, new TransformationNonCancellable<ATesselation>(agent) {
			@Override
			public void apply() {
				//TODO move somewhere else - constraint at tesselation level ?
				MorphologicalAnalysis.removeNarrowGapsTesselation(getAgent().getUnits(), separationDistance, 5, nodingDistance);
			}
		});
	}

}
