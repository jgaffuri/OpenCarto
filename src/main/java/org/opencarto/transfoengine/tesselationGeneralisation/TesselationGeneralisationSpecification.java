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
	public abstract void setUnitConstraints(ATesselation t, CartographicResolution res);
	public abstract void setTopologicalConstraints(ATesselation t, CartographicResolution res);
}
