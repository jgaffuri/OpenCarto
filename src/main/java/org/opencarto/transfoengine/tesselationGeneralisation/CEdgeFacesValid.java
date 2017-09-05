/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Face;
import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.Geometry;
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

	private boolean ok = true;

	@Override
	public void computeCurrentValue() {
		ok = true;
		Edge e = ((AEdge)getAgent()).getObject();
		ok = check(e.f1) && check(e.f2);
	}

	private boolean check(Face f) {
		if(!f.getGeometry().isSimple()) return false;
		if(!f.getGeometry().isValid()) return false;

		//check other faces intersecting g
		Geometry g = f.getGeometry(), g2;
		for(Object f2_ : faceSpatialIndex.query(g.getEnvelopeInternal())){
			g2 = ((Face)f2_).getGeometry();
			//TODO check geometries do not 'cross'
		}

		return true;
	}


	@Override
	public void computeSatisfaction() {
		satisfaction = ok ? 10 : 0;
	}

	@Override
	public boolean isHard() { return true; }

	@Override
	public List<Transformation<?>> getTransformations() {
		return new ArrayList<Transformation<?>>();
	}
}
