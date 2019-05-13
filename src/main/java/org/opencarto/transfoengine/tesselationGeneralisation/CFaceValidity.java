/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.algo.graph.FaceValidity;
import org.opencarto.transfoengine.Constraint;

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

	@Override
	public void computeSatisfaction() {
		boolean ok = FaceValidity.get(getAgent().getObject(), true, true);
		satisfaction = ok? 10 : 0;
	}

	@Override
	public boolean isHard() { return true; }

}
