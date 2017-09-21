/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
public class CUnitNoNarrowGap extends Constraint<AUnit> {
	//private final static Logger LOGGER = Logger.getLogger(CUnitNoNarrowPartsAndCorridors.class);

	private double resolution, sizeDel; int quad;
	public CUnitNoNarrowGap(AUnit agent, double resolution, double sizeDel, int quad) {
		super(agent);
	}

	private Collection<Polygon> gaps;

	@Override
	public void computeCurrentValue() {
		gaps = MorphologicalAnalysis.getNarrowGaps(getAgent(), resolution, sizeDel, quad);
	}

	@Override
	public void computeSatisfaction() {
	}

	@Override
	public List<Transformation<AUnit>> getTransformations() {
		ArrayList<Transformation<AUnit>> out = new ArrayList<Transformation<AUnit>>();
		return out;
	}

}
