/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.transfoengine.CartographicResolution;

/**
 * @author julien Gaffuri
 *
 */
public abstract class TesselationGeneralisationSpecification {
	CartographicResolution res;

	boolean preserveAllUnits = true;
	boolean preserveIfPointsInIt = true;
	boolean noTriangle = true;
	double nodingResolution = 1e-5;
	int quad = 4;

	public TesselationGeneralisationSpecification(CartographicResolution res) {
		this.res=res;
	}

	public abstract void setUnitConstraints(ATesselation t);
	public abstract void setTopologicalConstraints(ATesselation t);
}
