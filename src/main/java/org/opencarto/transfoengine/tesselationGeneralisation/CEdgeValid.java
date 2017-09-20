/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.index.SpatialIndex;

/**
 * Ensure the edge is valid:
 * 1. The edge do not self intersect (it is valid)
 * 2. The edge does not intersect other edges
 * 3. Both faces connected to the edge (if any) remain valid, that is:
 * - Their geometry is simple & valid
 * - They do not overlap other faces (this could happen when for example an edge is significantly simplified and a samll island becomes on the other side)
 * 
 * @author julien Gaffuri
 *
 */
public class CEdgeValid extends Constraint<AEdge> {
	private final static Logger LOGGER = Logger.getLogger(CEdgeValid.class);

	SpatialIndex faceSpatialIndex;

	public CEdgeValid(AEdge agent, SpatialIndex faceSpatialIndex) {
		super(agent);
		this.faceSpatialIndex = faceSpatialIndex;
	}

	private boolean ok = true;

	@Override
	public void computeCurrentValue() {
		ok = true;
		if(getAgent().isDeleted()) return;
		Edge e = getAgent().getObject();

		ok = e.isOK(false);
		if(!ok) return;

		if(e.f1 != null) ok = e.f1.isOK(false);
		if(!ok) return;

		if(e.f2 != null) ok = e.f2.isOK(false);

		if(!ok) LOGGER.debug("CEdgeFacesValid violated for "+getAgent().getId());
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
