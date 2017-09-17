/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.LineString;

/**
 * Ensure too short segment edges are collapsed or lengthened.
 * 
 * @author julien Gaffuri
 *
 */
public class CEdgeSize extends Constraint<AEdge> {
	double minSize, delSize;

	public CEdgeSize(AEdge agent, double minSize, double delSize) {
		super(agent);
		this.minSize = minSize;
		this.delSize = delSize;
	}

	double currentSize, goalSize;
	@Override
	public void computeCurrentValue() {
		currentSize = getAgent().getObject().getGeometry().getLength();
	}

	@Override
	public void computeGoalValue() {
		goalSize = currentSize>minSize ? currentSize : (currentSize<delSize)? 0.0001 : minSize;
	}

	@Override
	public void computeSatisfaction() {
		if(getAgent().isDeleted()) { satisfaction=10; return; }

		LineString g = getAgent().getObject().getGeometry();
		if(g.isClosed()) { satisfaction = 10; return; }

		satisfaction = 10 - 10*Math.abs(goalSize-currentSize)/goalSize;
		if(satisfaction<0) satisfaction=0;
	}

	@Override
	public List<Transformation<AEdge>> getTransformations() {
		ArrayList<Transformation<AEdge>> tr = new ArrayList<Transformation<AEdge>>();
		tr.add(new TEdgeChangeLength(getAgent(), goalSize/currentSize));
		return tr;
	}

}
