/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.transfoengine.Constraint;

import com.vividsolutions.jts.geom.LineString;

/**
 * Ensure an edge does not become a triangle.
 * 
 * @author julien Gaffuri
 *
 */
public class CEdgeTriangle extends Constraint<AEdge> {

	public CEdgeTriangle(AEdge agent) { super(agent); }

	boolean isTriangleIni = false;
	boolean isTriangle = false;

	@Override
	public void computeCurrentValue() {
		LineString g = getAgent().getObject().getGeometry();
		if(g.isClosed() && g.getNumPoints()<=4)
			isTriangle = true;
		else
			isTriangle = false;
	}

	@Override
	public void computeInitialValue() {
		computeCurrentValue();
		isTriangleIni = isTriangle;
	}

	@Override
	public void computeSatisfaction() {
		satisfaction = isTriangleIni? 10 : isTriangle? 0 : 10;
	}

	@Override
	public boolean isHard() { return true; }

}
