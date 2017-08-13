/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

/**
 * @author julien Gaffuri
 *
 */
public class CEdgeMinimumSize extends Constraint {

	private double minSizeDel, minSize;

	public CEdgeMinimumSize(Agent agent, double minSizeDel, double minSize) {
		super(agent);
		this.minSizeDel=minSizeDel;
		this.minSize=minSize;
	}

	double currentValue, goalValue;

	@Override
	public void computeCurrentValue() {
		Edge d = (Edge)(getAgent().getObject());
		currentValue = d.getGeometry()==null? 0 : d.getGeometry().getLength();
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
	public List<Transformation<?>> getTransformations() {
		ArrayList<Transformation<?>> out = new ArrayList<Transformation<?>>();
		//TODO add edge collapse and edge lengthening
		return out;
	}

}
