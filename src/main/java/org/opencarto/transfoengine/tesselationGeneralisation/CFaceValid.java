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
		isValid = f.isValid();
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
