/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.transfoengine.Constraint;

/**
 * Ensures that the face contains some specified points.
 * 
 * @author julien Gaffuri
 *
 */
public class CFaceContainPoints extends Constraint<AFace> {

	public CFaceContainPoints(AFace agent) {
		super(agent);
	}

	@Override
	public void computeSatisfaction() {
		satisfaction = getAgent().containPoints()? 10 : 0;
	}

	@Override
	public boolean isHard() { return true; }

}
