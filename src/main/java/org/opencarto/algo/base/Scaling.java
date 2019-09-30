/**
 * 
 */
package org.opencarto.algo.base;

import org.locationtech.jts.geom.Coordinate;

/**
 * 
 * @author julien Gaffuri
 *
 */
public class Scaling {

	public static void applyScaling(Coordinate coord, Coordinate center, double coef){
		coord.x = center.x + coef*(coord.x-center.x);
		coord.y = center.y + coef*(coord.y-center.y);
	}

}
