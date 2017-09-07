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
		Polygon poly = ((Face)getAgent().getObject()).getGeometry();
		isValid = poly.isValid() && poly.isSimple();

		//TODO check does not intersects other faces
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
