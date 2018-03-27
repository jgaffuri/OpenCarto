/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

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

	public CUnitNoNarrowGaps(AUnit agent) {
		super(agent);
	}

	@Override
	public void computeCurrentValue() {
		//compute narrow gaps
	}

	@Override
	public void computeSatisfaction() {
		//compute satisfaction as a ratio of surfaces
	}

	@Override
	public List<Transformation<AUnit>> getTransformations() {
		ArrayList<Transformation<AUnit>> out = new ArrayList<Transformation<AUnit>>();

		//transformation which iterativelly fill the gaps, checking the point thing
		//rebuild noding in the end

		return out;
	}

}
