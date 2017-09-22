package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.algo.polygon.MorphologicalAnalysis;
import org.opencarto.transfoengine.TransformationNonCancellable;

public class TUnitNarrowGapsFilling extends TransformationNonCancellable<AUnit> {

	private double resolution, sizeDel; private int quad;
	public TUnitNarrowGapsFilling(AUnit a, double resolution, double sizeDel, int quad) {
		super(a);
		this.resolution = resolution;
		this.sizeDel = sizeDel;
		this.quad = quad;
	}


	@Override
	public void apply() {
		getAgent().getObject().setGeom(
				MorphologicalAnalysis.fillNarrowGaps(getAgent().getObject().getGeom(), resolution, sizeDel, quad)
				);
	}

}
