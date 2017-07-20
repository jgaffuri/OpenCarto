/**
 * 
 */
package org.opencarto.algo.measure;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/**
 * Granularity measure based on segments' length statistics
 * 
 * @author julien Gaffuri
 *
 */
public class Granularity {

	public static class Measurement {
		double min, max, average;
		int nb;
	}


	public static Measurement get(LineString ls){
		Measurement m = new Measurement();

		m.nb = ls.getNumPoints()-1;
		m.average = ls.getLength()/m.nb;

		m.min = Double.MAX_VALUE; m.max = 0;
		Coordinate c0 = ls.getCoordinateN(0);
		for(int i=1; i<=m.nb; i++){
			Coordinate c1 = ls.getCoordinateN(i);
			double length = c1.distance(c0);
			m.min = Math.min(length, m.min);
			m.max = Math.max(length, m.max);
			c0 = c1;
		}

		return m;
	}


}
