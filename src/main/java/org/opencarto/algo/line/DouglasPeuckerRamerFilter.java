/**
 * 
 */
package org.opencarto.algo.line;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;

/**
 * @author julien Gaffuri
 *
 */
public class DouglasPeuckerRamerFilter {
	static Logger logger = Logger.getLogger(DouglasPeuckerRamerFilter.class.getName());

	public static Geometry get(Geometry g, double d){
		if (d < 0.0) {
			logger.warning("Distance tolerance must be positive: " + d);
			return (Geometry)g.clone();
		}

		if (d == 0.0) return (Geometry)g.clone();

		Geometry g_;
		try {
			g_ = DouglasPeuckerSimplifier.simplify(g, d);
			if( g_ == null || g_.isEmpty() || !g_.isValid() || g_.getGeometryType() != g.getGeometryType() )
				g_ = TopologyPreservingSimplifier.simplify(g, d);
			else
				return g_;
		} catch (Exception e) {
			return (Geometry)g.clone();
		}

		if (g_ == null) {
			logger.warning("Null geometry");
			return (Geometry)g.clone();
		} else if (g_.getGeometryType() != g.getGeometryType()) {
			logger.warning("Different types of geometry");
			//System.out.println(g.getGeometryType() + "   " + g_.getGeometryType());
			return (Geometry)g.clone();
		} else if (!g_.isValid()) {
			logger.info("Non valid geometry");
			return (Geometry)g.clone();
		} else if (g_.isEmpty() ) {
			logger.warning("Empty geometry");
			return (Geometry)g.clone();
		} else return g_;
	}

	public static ArrayList<Coordinate> getCoordinatesToRemove(Geometry geom, double dp) {
		Coordinate[] csToKeep = DouglasPeuckerSimplifier.simplify(geom, dp).getCoordinates();
		//Coordinate[] csToKeep = get(geom, dp).getCoordinates();
		ArrayList<Coordinate> cs = new ArrayList<Coordinate>();
		for(Coordinate c : geom.getCoordinates())
			if(!isIn(csToKeep, c)) cs.add(c);
		return cs;
	}

	private static boolean isIn(Coordinate[] cs, Coordinate c){
		for(Coordinate c_:cs)
			if(c_.x==c.x && c_.y==c.y) return true;
		return false;
	}

}
