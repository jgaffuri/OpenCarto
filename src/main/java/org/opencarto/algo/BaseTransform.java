/**
 * 
 */
package org.opencarto.algo;

import org.locationtech.jts.geom.Coordinate;

/**
 * Few basic transformation functions.
 * 
 * @author julien Gaffuri
 *
 */
public class BaseTransform {

	public static void applyScaling(Coordinate coord, Coordinate center, double coef){
		coord.x = center.x + coef*(coord.x-center.x);
		coord.y = center.y + coef*(coord.y-center.y);
	}

}
