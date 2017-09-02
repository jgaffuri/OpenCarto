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
 * Ensures an edge does not intersect itself (it should remain "simple").
 * 
 * @author julien Gaffuri
 *
 */
public class CEdgeNoSelfIntersection extends Constraint {

	public CEdgeNoSelfIntersection(Agent agent) {
		super(agent,null);
	}

	boolean selfIntersects = false;

	@Override
	public void computeCurrentValue() {
		selfIntersects = !((Edge)getAgent().getObject()).getGeometry().isSimple();
	}

	@Override
	public void computeSatisfaction() {
		satisfaction = selfIntersects? 0 : 10;
	}

	@Override
	public boolean isHard() { return true; }

	@Override
	public List<Transformation<?>> getTransformations() {
		return new ArrayList<Transformation<?>>();
	}

}
