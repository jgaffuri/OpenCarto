/**
 * 
 */
package org.opencarto.transfoengine;

import org.opencarto.datamodel.graph.Domain;

/**
 * @author julien Gaffuri
 *
 */
public class DomainSizeConstraint extends Constraint {

	private double minSizeDel, minSize;

	public DomainSizeConstraint(Agent agent, double minSizeDel, double minSize) {
		super(agent);
		this.minSizeDel=minSizeDel;
		this.minSize=minSize;
	}



	double currentValue, goalValue;

	@Override
	public void computeCurrentValue() {
		Domain d = (Domain)(getAgent().getObject());
		currentValue = d.getGeometry().getArea();
	}

	@Override
	public void computeGoalValue() {
		goalValue = currentValue>minSize ? currentValue : currentValue<minSizeDel? 0 : minSize;
	}




	@Override
	public void computeSatisfaction() {
		computeCurrentValue();
		computeGoalValue();

		if(getAgent().isDeleted())
			if(goalValue == 0) satisfaction=10; else satisfaction=0;
		else
			if(goalValue == 0) satisfaction=0;
			else satisfaction = 10 - 10*Math.abs(goalValue-currentValue)/goalValue;
	}

}
