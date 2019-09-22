/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.locationtech.jts.geom.MultiPolygon;
import org.opencarto.algo.polygon.Triangle;
import org.opencarto.transfoengine.Constraint;

/**
 * Ensure an edge does not become a triangle.
 * 
 * @author julien Gaffuri
 *
 */
public class CUnitNoTriangle extends Constraint<AUnit> {

	public CUnitNoTriangle(AUnit agent) { super(agent); }

	int nbTriangleIni = 0;
	@Override
	public void computeInitialValue() {
		nbTriangleIni = Triangle.nb((MultiPolygon) getAgent().getObject().getDefaultGeometry());
	}

	@Override
	public void computeSatisfaction() {
		int nbTriangle = Triangle.nb((MultiPolygon) getAgent().getObject().getDefaultGeometry());
		satisfaction = nbTriangle<=nbTriangleIni? 10 : 0;
	}

	@Override
	public boolean isHard() { return true; }

}
