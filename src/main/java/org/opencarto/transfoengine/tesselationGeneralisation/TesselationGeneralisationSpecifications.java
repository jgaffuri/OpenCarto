/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

/**
 * @author julien Gaffuri
 *
 */
public interface TesselationGeneralisationSpecifications {
	public void setUnitConstraints(ATesselation t, double resolution);
	public void setTopologicalConstraints(ATesselation t, double resolution);
}
