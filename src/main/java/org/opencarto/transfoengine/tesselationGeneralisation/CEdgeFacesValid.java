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
 * Ensures that both faces connected to the edge (if any) remain valid, that is:
 * - Their geometry is simple & valid
 * - They do not overlap other faces (this could happen when for example an edge is significantly simplified and a samll island becomes on the other side)
 * 
 * @author julien Gaffuri
 *
 */
public class CEdgeFacesValid extends Constraint<AEdge> {
	private final static Logger LOGGER = Logger.getLogger(CEdgeFacesValid.class);

	SpatialIndex faceSpatialIndex;

	public CEdgeFacesValid(AEdge agent, SpatialIndex faceSpatialIndex) {
		super(agent);
		this.faceSpatialIndex = faceSpatialIndex;
	}

	private boolean ok = true;

	@Override
	public void computeCurrentValue() {
		ok = true;
		if(getAgent().isDeleted()) return;
		Edge e = getAgent().getObject();

		if(e.f1 != null) ok = e.f1.isValid();
		if(e.f2 != null && ok) ok = e.f2.isValid();

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
