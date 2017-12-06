/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.apache.log4j.Logger;
import org.opencarto.transfoengine.Constraint;

import com.vividsolutions.jts.index.SpatialIndex;

/**
 * 
 * Check a unit is correctly noded to its touching ones.
 * 
 * @author julien
 *
 */
public class CUnitNoding  extends Constraint<AUnit> {
	private final static Logger LOGGER = Logger.getLogger(CUnitNoding.class);

	SpatialIndex index;

	public CUnitNoding(AUnit agent, SpatialIndex index) {
		super(agent);
		this.index = index;
	}

}
