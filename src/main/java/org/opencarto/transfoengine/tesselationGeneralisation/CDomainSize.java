/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.datamodel.graph.Domain;
import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

/**
 * @author julien Gaffuri
 *
 */
public class CDomainSize extends Constraint {

	private double minSizeDel, minSize;

	public CDomainSize(Agent agent, double minSizeDel, double minSize) {
		super(agent);
		this.minSizeDel=minSizeDel;
		this.minSize=minSize;
	}



	double initialValue, currentValue, goalValue;

	@Override
	public void computeInitialValue() {
		computeCurrentValue();
		initialValue = currentValue;
	}

	@Override
	public void computeCurrentValue() {
		Domain d = (Domain)(getAgent().getObject());
		currentValue = d.getGeometry()==null? 0 : d.getGeometry().getArea();
	}

	@Override
	public void computeGoalValue() {
		goalValue = currentValue>minSize ? currentValue : currentValue<minSizeDel? 0 : minSize;
	}




	@Override
	public void computeSatisfaction() {
		if(getAgent().isDeleted())
			if(goalValue == 0) satisfaction=10; else satisfaction=0;
		else
			if(goalValue == 0) satisfaction=0;
			else satisfaction = 10 - 10*Math.abs(goalValue-currentValue)/goalValue;
	}

	@Override
	public List<Transformation> getTransformations() {
		//TODO add transformation to remove small enclaves + islands + enlargement (?)
		return new ArrayList<Transformation>();
	}

}
