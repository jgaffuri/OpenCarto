package org.opencarto.algo.deformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opencarto.algo.deformation.base.GPoint;
import org.opencarto.algo.deformation.submicro.GAngle;
import org.opencarto.algo.deformation.submicro.GSegment;
import org.opencarto.algo.deformation.submicro.GSinglePoint;

public class Decomposers {
	private static Logger logger = Logger.getLogger(Decomposers.class.getName());

	public static GSinglePoint decompose(Collection<GPoint> ps, Point pt) {
		GPoint gp = new GPoint(pt.getCoordinate());
		ps.add(gp);
		return new GSinglePoint(gp);
	}

	public static void decomposeLimit(Polygon poly, double resolution, Collection<GPoint> ps, Collection<GSegment> segs, boolean buildAngles, Collection<GAngle> as) {
		decomposerDPL(poly.getExteriorRing().getCoordinates(), resolution, ps, segs, buildAngles, as);
		for (int i = 0; i < poly.getNumInteriorRing(); i++)
			decomposerDPL(poly.getInteriorRingN(i).getCoordinates(), resolution, ps, segs, buildAngles, as);
	}

	public static void decompose(LineString line, double resolution, Collection<GPoint> ps, Collection<GSegment> segs, boolean buildAngles, Collection<GAngle> as) {
		decomposerDPL(line.getCoordinates(), resolution, ps, segs, buildAngles, as);
	}

	public static Collection<GSinglePoint> createSinglePoints(Collection<GPoint> ps){
		ArrayList<GSinglePoint> sps = new ArrayList<GSinglePoint>();
		for (GPoint p : ps) sps.add(new GSinglePoint(p));
		return sps;
	}


	private static void decomposerDPL(Coordinate[] coords, double resolution, Collection<GPoint> ps, Collection<GSegment> segs, boolean buildAngles, Collection<GAngle> as) {
		int nb = coords.length;
		if(logger.isLoggable(Level.FINEST)) logger.log(Level.FINEST, "point nb=" + nb);

		//if there are less than 2 points, there is a problem
		if (nb < 2) {
			logger.severe("Error when decomposing. Coordinates list must have more than 2 points.");
			logger.severe(coords.toString());
			return;
		}

		//create the two first points and their segment
		GPoint p0 = new GPoint(coords[0]);
		ps.add(p0);
		GPoint p1 = new GPoint(coords[1]);
		ps.add(p1);
		segs.add(new GSegment(p0, p1));

		//store the two first points (usefull at the end, for angle construction)
		GPoint p0_ = p0;
		GPoint p1_ = p1;

		GPoint p2 = null;
		for (int i=2; i<nb-1; i++) {

			//build point
			if(logger.isLoggable(Level.FINEST)) logger.log(Level.FINEST, "(" + coords[i].x + ", " + coords[i].y + ")");
			p2 = new GPoint(coords[i]);
			ps.add(p2);

			//build segment
			segs.add(new GSegment(p1, p2));

			//build angle (if needed)
			if (buildAngles) as.add( new GAngle(p0, p1, p2) );

			//next
			p0 = p1;
			p1 = p2;
		}

		//test closure
		boolean closed;
		if(coords[0].distance(coords[nb-1]) <= resolution) closed = true; else closed = false;

		if (closed) {
			//build the last segment to close the ring
			segs.add(new GSegment(p1, p0_));
			//build the two last angles (if needed)
			if (buildAngles) {
				as.add( new GAngle(p0, p1, p0_) );
				as.add( new GAngle(p1, p0_, p1_) );
			}
			//possible link between last coordinate and first point agent
			if(coords[0] != coords[nb-1]) p0_.getCoordinates().add( coords[nb-1] );
		} else {
			//build the last point
			if(logger.isLoggable(Level.FINEST)) logger.log(Level.FINEST, "(" + coords[nb-1].x + ", " + coords[nb-1].y + ")");
			p2 = new GPoint(coords[nb-1]);
			ps.add(p2);

			//build the last segment
			segs.add(new GSegment(p1, p2));

			//build the last angle (if needed)
			if (buildAngles) as.add( new GAngle(p0, p1, p2) );
		}
	}
}
