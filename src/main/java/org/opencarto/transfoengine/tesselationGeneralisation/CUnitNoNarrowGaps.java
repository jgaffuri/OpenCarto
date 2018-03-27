/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.opencarto.algo.polygon.MorphologicalAnalysis;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.Polygon;

/**
 * 
 * Constraint ensuring that a unit has no narrow gap.
 * Gaps are detected on-the-fly. It is assumed that a narrow part is the complementary
 * 
 * @author julien Gaffuri
 *
 */
public class CUnitNoNarrowGaps extends Constraint<AUnit> {
	private final static Logger LOGGER = Logger.getLogger(CUnitNoNarrowGaps.class.getName());

	private double separationDistanceMeter;
	private int quad;
	public CUnitNoNarrowGaps(AUnit agent, double separationDistanceMeter, int quad) {
		super(agent);
		this.separationDistanceMeter = separationDistanceMeter;
		this.quad = quad;
	}

	//the narrow gaps
	private Collection<Polygon> ngs;

	@Override
	public void computeCurrentValue() {
		//compute narrow gaps
		ngs = MorphologicalAnalysis.getNarrowGaps(getAgent().getObject().getGeom(), separationDistanceMeter, quad);
	}

	@Override
	public void computeSatisfaction() {
		//depends on the size of the narrow gaps
		double a = getAgent().getObject().getGeom().getArea();
		if(a==0) { satisfaction = 10; return; }
		double snga=0; for(Polygon ng : ngs) snga += ng.getArea();
		satisfaction = 10*(1-snga/a);
		satisfaction = satisfaction>10? 10 : satisfaction<0? 0 : satisfaction;
	}

	@Override
	public List<Transformation<AUnit>> getTransformations() {
		ArrayList<Transformation<AUnit>> out = new ArrayList<Transformation<AUnit>>();
		out.add(new T(getAgent()));
		return out;
	}

	private static class T extends Transformation<AUnit> {

		public T(AUnit agent) {
			super(agent);
		}

		@Override
		public void apply() {
			//TODO
			//transformation which iterativelly fill the gaps, checking the point thing
			//rebuild noding in the end
		}

		@Override
		public boolean isCancelable() { return false; }
	}

}
