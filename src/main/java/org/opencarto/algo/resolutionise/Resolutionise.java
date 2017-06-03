/**
 * 
 */
package org.opencarto.algo.resolutionise;

import org.opencarto.algo.base.Copy;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
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
public class Resolutionise {
	public Geometry punctual = null;
	public Geometry linear = null;
	public Geometry aeral = null;

	public Resolutionise(Geometry g, double resolution){
		if(g instanceof GeometryCollection){
			System.out.println("Resolutionise non implemented yet for GeometryCollection");
		} else if(g.getArea() > 0) {
			System.out.println("Resolutionise non implemented yet for areas");
		} else if(g.getLength() > 0) {
			System.out.println("Resolutionise non implemented yet for lines");
		} else {
			if(g instanceof Point){
				System.out.println("Resolutionise non implemented yet for point");
			} else {
				System.out.println("Resolutionise non implemented yet for mulipoint");
			}
		}
	}


	public static  Geometry perform(Geometry g, double resolution) {
		Geometry out = Copy.perform(g);

		apply(out.getCoordinates(), resolution);

		if(out instanceof Point) ;
		else if(out instanceof MultiPoint) ;
		else if(out instanceof LineString) ;
		else if(out instanceof MultiLineString) ;
		else if(out instanceof Polygon) out = out.buffer(0);
		else if(out instanceof MultiPolygon) out = out.buffer(0);

		return out;
	}

	public static Coordinate perform(Coordinate c, double resolution){
		return new Coordinate(
				Math.round(c.x/resolution)*resolution,
				Math.round(c.y/resolution)*resolution
				);
	}
	public static Coordinate[] perform(Coordinate[] cs, double resolution){
		Coordinate[] cs_ = new Coordinate[cs.length];
		for(int i=0; i<cs.length; i++) cs_[i] = perform(cs[i], resolution);
		return cs_;
	}

	public static void apply(Coordinate c, double resolution){
		c.x = Math.round(c.x/resolution)*resolution;
		c.y = Math.round(c.y/resolution)*resolution;
	}
	public static void apply(Coordinate[] cs, double resolution){ for(Coordinate c : cs) apply(c, resolution); }

}
