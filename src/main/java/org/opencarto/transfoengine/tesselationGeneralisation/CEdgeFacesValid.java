/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.index.SpatialIndex;

/**
 * Ensures that both faces connected to an edge (if any) remain valid, that is:
 * - Their geometry is valid
 * - They do not intersect other faces (this could happen when for example an edge is significantly simplified and a samll island becomes on the other side)
 * 
 * @author julien Gaffuri
 *
 */
public class CEdgeFacesValid extends Constraint {
	SpatialIndex faceSpatialIndex;

	public CEdgeFacesValid(Agent agent, SpatialIndex faceSpatialIndex) {
		super(agent);
		this.faceSpatialIndex = faceSpatialIndex;
	}

	boolean intersectsOthers = false;
	boolean isValid = true;

	@Override
	public void computeCurrentValue() {
	}

	@Override
	public void computeSatisfaction() {
		satisfaction = intersectsOthers||!isValid ? 0 : 10;
	}

	@Override
	public boolean isHard() { return true; }

	@Override
	public List<Transformation<?>> getTransformations() {
		return new ArrayList<Transformation<?>>();
	}
}
