/**
 * 
 */
package org.opencarto.transfoengine.statUnitsGeneralisation;

import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.Constraint;

/**
 * @author julien Gaffuri
 *
 */
public class EdgeNoSelfIntersection extends Constraint {

	public EdgeNoSelfIntersection(Agent agent) {
		super(agent);
	}

	@Override
	public void computeCurrentValue() {}

	@Override
	public void computeGoalValue() {}

	@Override
	public void computeSatisfaction() {}

	@Override
	public boolean isHard() { return true; }

}
