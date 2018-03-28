/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.Constraint;

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
		boolean ok =
				(e.f1==null || e.f1.containPoints(getAgent().getAtesselation().getAFace(e.f1).points))
				&&
				(e.f2==null || e.f2.containPoints(getAgent().getAtesselation().getAFace(e.f2).points))
				;
		satisfaction = ok? 10 : 0;
	}

	@Override
	public boolean isHard() { return true; }

}
