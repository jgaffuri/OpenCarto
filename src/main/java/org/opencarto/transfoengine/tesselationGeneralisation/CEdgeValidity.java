/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

/**
 * Ensure the edge is valid:
 * 1. The edge do not self intersect (it is simple)
 * 2. The edge does not intersect other edges
 * 3. Both faces connected to the edge (if any) remain valid, that is:
 * - Their geometry is simple & valid
 * - They do not overlap other faces (this could happen when for example an edge is significantly simplified and a samll island becomes on the other side)
 * 
 * @author julien Gaffuri
 *
 */
public class CEdgeValidity extends Constraint<AEdge> {
	//private final static Logger LOGGER = Logger.getLogger(CEdgeValidity.class);

	public CEdgeValidity(AEdge agent) {
		super(agent);
	}

	private boolean ok = true;

	@Override
	public void computeCurrentValue() {
		ok = true;
		if(getAgent().isDeleted()) return;
		Edge e = getAgent().getObject();

		ok = e.isOK(false, false);
		if(!ok) return;

		if(e.f1 != null) ok = e.f1.isOK(false, false);
		if(!ok) return;

		if(e.f2 != null) ok = e.f2.isOK(false, false);
		if(!ok) return;
	}


	@Override
	public void computeSatisfaction() {
		satisfaction = getAgent().isDeleted()? 10 : ok ? 10 : 0;
	}

	@Override
	public boolean isHard() { return true; }

	@Override
	public List<Transformation<AEdge>> getTransformations() {
		return new ArrayList<Transformation<AEdge>>();
	}
}
