/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

/**
 * @author julien Gaffuri
 *
 */
public class CUnitValid  extends Constraint<AUnit> {

	boolean valid;

	public CUnitValid(AUnit agent) { super(agent); }

	@Override
	public void computeCurrentValue() {
		LOGGER.info("CUnitValid "+getAgent().getObject().id);
		valid = getAgent().getObject().getGeom().isValid();
	}

	@Override
	public void computeSatisfaction() {
		satisfaction = valid? 10 : 0;
	}

	@Override
	public List<Transformation<AUnit>> getTransformations() {
		return new ArrayList<Transformation<AUnit>>();
	}

}
