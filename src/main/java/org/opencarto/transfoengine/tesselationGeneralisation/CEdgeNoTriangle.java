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
public class CEdgeNoTriangle extends Constraint<AEdge> {

	public CEdgeNoTriangle(AEdge agent) { super(agent); }

	boolean isTriangleIni = false;
	@Override
	public void computeInitialValue() {
		isTriangleIni = Triangle.is(getAgent().getObject().getGeometry());
		//if(isTriangleIni) System.out.println("ini "+getAgent().getObject().getN1().getC());
	}

	@Override
	public void computeSatisfaction() {
		satisfaction = isTriangleIni? 10 : Triangle.is(getAgent().getObject().getGeometry())? 0 : 10;
		if(satisfaction == 0) System.out.println("cur "+getAgent().getObject().getN1().getC());
	}

	@Override
	public boolean isHard() { return true; }

}
