/**
 * 
 */
package org.opencarto.gisco.rail;

import org.locationtech.jts.geom.LineString;

/**
 * 
 * Build a line representing the average of two lines.
 * This can be usefull when both lines are very similar and an aggregated version is needed.
 * It is simpler than computing a squeletton based central line.
 * The line similarity can be found with the hausdorf distance or (if both have same initial/final points) with surface elongation measure (Elongation.getWidthApproximation).
 * 
 * @author julien Gaffuri
 *
 */
public class LineStringAverage {
	
	
	

	public static LineString get(LineString ls1, LineString ls2) {
		LineString ls = null;
		//TODO
		return ls ;
	}
}
