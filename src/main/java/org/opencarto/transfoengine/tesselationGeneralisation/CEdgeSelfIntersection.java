/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

/**
 * Ensures an edge does not intersect itself (it should remain "simple").
 * 
 * @author julien Gaffuri
 *
 */
public class CEdgeSelfIntersection extends Constraint<AEdge> {

	public CEdgeSelfIntersection(AEdge agent) {
		super(agent);
	}

	boolean selfIntersects = false;

	@Override
	public void computeCurrentValue() {
		selfIntersects = !getAgent().getObject().getGeometry().isSimple();
	}

	@Override
	public void computeSatisfaction() {
		satisfaction = selfIntersects? 0 : 10;
	}

	@Override
	public boolean isHard() { return true; }

	@Override
	public List<Transformation<AEdge>> getTransformations() {
		return new ArrayList<Transformation<AEdge>>();
	}

}
