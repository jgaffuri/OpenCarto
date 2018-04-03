/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.opencarto.algo.polygon.MorphologicalAnalysis;
import org.opencarto.datamodel.Feature;
import org.opencarto.transfoengine.ConstraintOneShot;
import org.opencarto.transfoengine.TransformationNonCancellable;

/**
 * @author julien Gaffuri
 *
 */
public class CTesselationRemoveNarrowGaps extends ConstraintOneShot<ATesselation> {

	public CTesselationRemoveNarrowGaps(ATesselation agent, double separationDistance, double nodingResolution, int quad) {
		super(agent, new TransformationNonCancellable<ATesselation>(agent) {
			@Override
			public void apply() {
				//TODO move somewhere else - constraint at unit level ?
				List<Feature> units = new ArrayList<Feature>();
				units.addAll(getAgent().getUnits());

				units.sort(new Comparator<Feature>() {
					public int compare(Feature f1, Feature f2) {
						if(!f1.id.contains("EEZ")) return -1;
						if(!f2.id.contains("EEZ")) return 1;
						return 0;
					}
				});

				MorphologicalAnalysis.removeNarrowGapsTesselation(units, separationDistance, quad, nodingResolution);
			}
		});
	}

}
