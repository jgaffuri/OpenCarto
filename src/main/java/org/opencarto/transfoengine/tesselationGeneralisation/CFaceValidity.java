/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.datamodel.graph.Face;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

/**
 * Ensures that the face remain valid, that is its geometry is simple & valid, and it does not overlap any other face of the tesselation.
 * 
 * @author julien Gaffuri
 *
 */
public class CFaceValidity extends Constraint<AFace> {

	public CFaceValidity(AFace agent) {
		super(agent);
	}

	private boolean ok = true;

	@Override
	public void computeCurrentValue() {
		Face f = getAgent().getObject();
		ok = f.isOK(true, true);
	}


	@Override
	public void computeSatisfaction() {
		satisfaction = ok? 10 : 0;
	}

	@Override
	public boolean isHard() { return true; }

	@Override
	public List<Transformation<AFace>> getTransformations() {
		return new ArrayList<Transformation<AFace>>();
	}
}
