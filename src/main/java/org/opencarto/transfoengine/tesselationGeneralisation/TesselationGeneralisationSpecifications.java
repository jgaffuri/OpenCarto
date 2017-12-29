/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

/**
 * @author julien Gaffuri
 *
 */
public interface TesselationGeneralisationSpecifications {
	public void setUnitConstraints(ATesselation t, double perceptionLengthMeter, double perceptionSizeSqMeter);
	public void setTopologicalConstraints(ATesselation t, double perceptionLengthMeter, double perceptionSizeSqMeter);
}
