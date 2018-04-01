/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.Constraint;

/**
 * Ensure the edge is valid:
 * 1. The edge do not self intersect (it is simple)
 * 2. Both faces connected to the edge (if any) remain valid, that is:
 * - Their geometry is simple & valid
 * - They do not overlap other faces (this could happen when for example an edge is significantly simplified and a samll island becomes on the other side)
 * 
 * @author julien Gaffuri
 *
 */
public class CEdgeValidity extends Constraint<AEdge> {
	//private final static Logger LOGGER = Logger.getLogger(CEdgeValidity.class.getName());

	public CEdgeValidity(AEdge agent) {
		super(agent);
	}

	@Override
	public void computeSatisfaction() {
		Edge e = getAgent().getObject();
		boolean ok = 
				e.isOK(false, false)
				&&
				(e.f1 == null || e.f1.isOK(true, true))
				&&
				(e.f2 == null || e.f2.isOK(true, true));
		;
		satisfaction = ok ? 10 : 0;
	}

	@Override
	public boolean isHard() { return true; }

}
