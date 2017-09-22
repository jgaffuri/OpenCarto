package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.algo.polygon.MorphologicalAnalysis;
import org.opencarto.transfoengine.Transformation;

public class TUnitNarrowGapsFilling extends Transformation<AUnit> {

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

	@Override
	public boolean isCancelable() { return false; }

	@Override
	public void storeState() {}

	@Override
	public void cancel() {
		System.err.println("cancel() not implemented for "+this.getClass().getSimpleName());
	}

}
