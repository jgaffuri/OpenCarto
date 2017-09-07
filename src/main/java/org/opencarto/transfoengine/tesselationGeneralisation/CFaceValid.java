/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.datamodel.graph.Face;
import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.Polygon;

/**
 * Ensures that none of the edges of the face intersects other edges.
 * 
 * @author julien Gaffuri
 *
 */
public class CFaceValid extends Constraint {

	public CFaceValid(Agent agent) {
		super(agent);
	}

	private boolean isValid = true;

	@Override
	public void computeCurrentValue() {
		Face f = (Face)getAgent().getObject();
		Polygon g = f.getGeometry();

		//check geometry validity
		isValid = g.isValid() && g.isSimple();
		if(!isValid) return;

		//check face does not intersects other faces
		for(Object f2_ : f.getGraph().getSpatialIndexFace().query(g.getEnvelopeInternal())){
			Face f2 = (Face)f2_;
			if(f==f2) continue;
			Polygon g2 = f2.getGeometry();

			if(!g2.getEnvelopeInternal().intersects(g.getEnvelopeInternal())) continue;

			try {
				if(!g2.intersects(g)) continue;
				if(g2.touches(g)) continue;
				isValid = false;
				return;
			} catch (Exception e) {
				isValid = false;
				return;
			}
		}
		isValid = true;
	}

	@Override
	public void computeSatisfaction() {
		satisfaction = isValid? 10 : 0;
	}

	@Override
	public boolean isHard() { return true; }

	@Override
	public List<Transformation<?>> getTransformations() {
		return new ArrayList<Transformation<?>>();
	}
}
