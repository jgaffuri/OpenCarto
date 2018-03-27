/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.Collection;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.Constraint;

import com.vividsolutions.jts.geom.Point;

/**
 * Ensures that the faces on both sides of the edge (if any) contain some specified points.
 * 
 * @author julien Gaffuri
 *
 */
public class CEdgesFacesContainPoints extends Constraint<AEdge> {

	public CEdgesFacesContainPoints(AEdge agent) {
		super(agent);
	}

	@Override
	public void computeSatisfaction() {
		Edge e = getAgent().getObject();
		boolean ok = CFaceContainPoints.checkFaceContainPoints(e.f1, ptDataF1) && CFaceContainPoints.checkFaceContainPoints(e.f2, ptDataF2);
		satisfaction = ok? 10 : 0;
	}

	@Override
	public boolean isHard() { return true; }

}
