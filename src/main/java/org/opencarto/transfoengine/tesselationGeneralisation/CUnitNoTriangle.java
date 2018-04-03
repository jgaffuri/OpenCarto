/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.algo.polygon.Triangle;
import org.opencarto.transfoengine.Constraint;

import com.vividsolutions.jts.geom.MultiPolygon;

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
		nbTriangleIni = Triangle.nb((MultiPolygon) getAgent().getObject().getGeom());
	}

	@Override
	public void computeSatisfaction() {
		int nbTriangle = Triangle.nb((MultiPolygon) getAgent().getObject().getGeom());
		satisfaction = nbTriangle<=nbTriangleIni? 10 : 0;
	}

	@Override
	public boolean isHard() { return true; }

}
