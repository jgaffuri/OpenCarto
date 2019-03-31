package org.opencarto.algo.deformation.constraint;

import java.util.logging.Logger;

import org.locationtech.jts.geom.Coordinate;
import org.opencarto.algo.deformation.base.GPoint;
import org.opencarto.algo.deformation.base.GSimpleConstraint;
import org.opencarto.algo.deformation.submicro.GSegment;

public class SegmentOrientation extends GSimpleConstraint {
	private static Logger logger = Logger.getLogger(SegmentOrientation.class.getName());

	private GSegment s;
	private double goal;

	public SegmentOrientation(GSegment s, double imp){
		this(s, imp, s.getPt1().getIniOrientation(s.getPt2()));
	}

	public SegmentOrientation(GSegment s, double imp, double goalOrientation){
		super(s, imp);
		this.s = s;
		this.goal = goalOrientation;
	}

	@Override
	public Coordinate getDisplacement(GPoint pt, double alpha) {
		double angle = alpha*s.getOrientationGap(this.goal);
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		double dx = 0.5 * (s.getPt2().getX()-s.getPt1().getX() + cos*(s.getPt1().getX()-s.getPt2().getX()) + sin*(s.getPt1().getY()-s.getPt2().getY()));
		double dy = 0.5 * (s.getPt2().getY()-s.getPt1().getY() - sin*(s.getPt1().getX()-s.getPt2().getX()) + cos*(s.getPt1().getY()-s.getPt2().getY()));
		if (pt == s.getPt1()) return new Coordinate(dx, dy);
		else if (pt == s.getPt2()) return new Coordinate(-dx, -dy);
		else {
			logger.severe("Error: point is not one of the segment's ones");
			return null;
		}
	}
}
