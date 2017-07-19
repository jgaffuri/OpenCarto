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

	public DomainSizeConstraint(Object object, double minSizeDel, double minSize) {
		super(object);
		this.minSizeDel=minSizeDel;
		this.minSize=minSize;
	}

	double currentValue, goalValue;

	@Override
	public void computeCurrentValue() {
		Domain d = (Domain)getObject();
		currentValue = d.getGeometry().getArea();
	}

	@Override
	public void computeGoalValue() {
		goalValue = currentValue>minSize ? currentValue : currentValue<minSizeDel? 0 : minSize;
	}


	@Override
	public void computeStatisfaction() {
		
	}

}
