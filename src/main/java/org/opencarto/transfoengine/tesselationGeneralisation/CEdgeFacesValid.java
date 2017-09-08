/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Face;
import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
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
	private final static Logger LOGGER = Logger.getLogger(CEdgeFacesValid.class);

	SpatialIndex faceSpatialIndex;

	public CEdgeFacesValid(Agent agent, SpatialIndex faceSpatialIndex) {
		super(agent);
		this.faceSpatialIndex = faceSpatialIndex;
	}

	private boolean ok = true;

	@Override
	public void computeCurrentValue() {
		ok = true;
		if(getAgent().isDeleted()) return;
		Edge e = ((AEdge)getAgent()).getObject();
		ok = isOK(e.f1);
		if(ok) ok = isOK(e.f2);

		if(!ok) LOGGER.debug("CEdgeFacesValid violated for "+getAgent().getId());
	}

	private boolean isOK(Face f) {
		if(f==null) return true;
		Polygon g = f.getGeometry();
		if(g==null) return false;
		if(!g.isValid()) return false;
		if(!g.isSimple()) return false;

		//check other faces intersecting face
		//System.out.println( faceSpatialIndex.query(g.getEnvelopeInternal()).size() );
		Geometry g2;
		for(Object f2_ : faceSpatialIndex.query(g.getEnvelopeInternal())){
			if(f2_==f) continue;
			g2 = ((Face)f2_).getGeometry();
			if(g2 == null){
				LOGGER.warn("Null geometry found for face "+((Face)f2_).getId());
				continue;
			}
			if(!g2.getEnvelopeInternal().intersects(g.getEnvelopeInternal())) continue;
			//if(!g2.intersects(g)) continue;
			//if(g2.touches(g)) continue;
			try {
				//TODO improve that - we only need cross/overlap
				Geometry inter = g.intersection(g2);
				if(inter==null || inter.isEmpty()) continue;
				if(inter.getArea()>0) return false;
			} catch (Exception e) {
				LOGGER.warn("Could not compute intersection in "+this.getClass().getSimpleName()+": "+e.getMessage());
				return false;
			}
		}

		return true;
	}


	@Override
	public void computeSatisfaction() {
		satisfaction = getAgent().isDeleted()? 10 : ok ? 10 : 0;
	}

	@Override
	public boolean isHard() { return true; }

	@Override
	public List<Transformation<?>> getTransformations() {
		return new ArrayList<Transformation<?>>();
	}
}
