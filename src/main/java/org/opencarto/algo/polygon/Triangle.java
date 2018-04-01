/**
 * 
 */
package org.opencarto.algo.polygon;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author julien Gaffuri
 *
 */
public class Triangle {

	public static boolean is(LineString ls) {
		return (ls.isClosed() && ls.getNumPoints()<=5);
	}

	public static boolean is(Polygon p) {
		if(p.getNumInteriorRing()>0) return false;
		return is(p.getExteriorRing());
	}
}
