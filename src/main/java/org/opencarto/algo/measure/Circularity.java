package org.opencarto.algo.measure;

import com.vividsolutions.jts.geom.LinearRing;

/**
 * Maesure if a shape is close to a circle or not.
 * 
 * @author julien Gaffuri
 *
 */
public class Circularity {

	/**
	 * @param lr
	 * @return 0 for a circle, 0.12838 for a square, and more for non-circular shapes
	 */
	public static double get(LinearRing lr) {
		return lr.getLength()/(2*Math.sqrt(Math.PI*lr.getArea())) - 1;
	}
}
