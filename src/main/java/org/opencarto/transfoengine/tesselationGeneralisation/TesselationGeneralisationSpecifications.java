/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.transfoengine.CartographicResolution;

/**
 * @author julien Gaffuri
 *
 */
public interface TesselationGeneralisationSpecifications {
	public void setUnitConstraints(ATesselation t, CartographicResolution res);
	public void setTopologicalConstraints(ATesselation t, CartographicResolution res);
}
