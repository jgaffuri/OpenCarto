/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.algo.polygon.Triangle;
import org.opencarto.transfoengine.Constraint;

/**
 * Ensure an edge does not become a triangle.
 * 
 * @author julien Gaffuri
 *
 */
public class CEdgeTriangle extends Constraint<AEdge> {

	public CEdgeTriangle(AEdge agent) { super(agent); }

	boolean isTriangleIni = false;
	@Override
	public void computeInitialValue() {
		isTriangleIni = Triangle.is(getAgent().getObject().getGeometry());
	}

	@Override
	public void computeSatisfaction() {
		satisfaction = isTriangleIni? 10 : Triangle.is(getAgent().getObject().getGeometry())? 0 : 10;
	}

	@Override
	public boolean isHard() { return true; }

}
