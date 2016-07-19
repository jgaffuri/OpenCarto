/**
 * 
 */
package org.opencarto.algo.measure;

import org.opencarto.algo.base.SmallestSurroundingRectangle;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

/**
 * 
 * @author julien Gaffuri
 *
 */
public class Elongation {

	// 0: line, 1: perfect square/circle
	public static double get(Geometry geom){
		Polygon ssr = SmallestSurroundingRectangle.get(geom, geom.getFactory());
		if (ssr == null) return 0;

		Coordinate[] coords = ssr.getCoordinates();
		Coordinate c1 = coords[1];
		double lg1 = coords[0].distance(c1);
		double lg2 = c1.distance(coords[2]);
		if (lg1>lg2) return Math.round(100*lg2/lg1)/100.0;
		return Math.round(100*lg1/lg2)/100.0;
	}

}
