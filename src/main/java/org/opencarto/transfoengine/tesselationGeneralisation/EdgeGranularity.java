/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.Constraint;

/**
 * @author julien Gaffuri
 *
 */
public class EdgeGranularity extends Constraint {
	double goalResolution, currentResolution;

	public EdgeGranularity(Agent agent, double goalResolution) {
		super(agent);
		this.goalResolution = goalResolution;
	}



	@Override
	public void computeCurrentValue() {
		//TODO
		currentResolution = goalResolution;
	}

	@Override
	public void computeGoalValue() {}

	@Override
	public void computeSatisfaction() {
		if(getAgent().isDeleted()) { satisfaction=10; return; }
		if(currentResolution>=goalResolution) { satisfaction=10; return; }
		satisfaction = 10-10*Math.abs(goalResolution-currentResolution)/goalResolution;
	}

}
