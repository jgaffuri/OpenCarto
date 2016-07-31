/**
 * 
 */
package org.opencarto.algo.integrate;

import org.opencarto.algo.base.Copy;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author julien Gaffuri
 *
 */
public class Integrate {
	//TODO check for all geometry types

	public static  Geometry perform(Geometry g, double distance) {
		Geometry out = Copy.perform(g);

		for(Coordinate c : out.getCoordinates()){
			c.x = Math.round(c.x/distance)*distance;
			c.y = Math.round(c.y/distance)*distance;
		}

		if(out instanceof Point) ;
		else if(out instanceof MultiPoint) ;
		else if(out instanceof LineString) ;
		else if(out instanceof MultiLineString) ;
		else if(out instanceof Polygon) out = out.buffer(0);
		else if(out instanceof MultiPolygon) out = out.buffer(0);

		return out;
	}

}
