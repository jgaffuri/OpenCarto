package org.opencarto.algo.deformation.constraint;

import java.util.logging.Logger;

import org.opencarto.algo.deformation.base.GPoint;
import org.opencarto.algo.deformation.base.GSimpleConstraint;
import org.opencarto.algo.deformation.submicro.GSegment;

import com.vividsolutions.jts.geom.Coordinate;

public class SegmentMinimalLength extends GSimpleConstraint {
	private static Logger logger = Logger.getLogger(SegmentMinimalLength.class.getName());

	private GSegment s;
	private double goalLength;

	public SegmentMinimalLength(GSegment s, double imp){
		this(s, imp, s.getIniLength());
	}

	public SegmentMinimalLength(GSegment s, double imp, double goalLength){
		super(s,imp);
		this.s = s;
		this.goalLength = goalLength;
	}

	@Override
	public Coordinate getDisplacement(GPoint p, double alpha) {
		double d,a,dx,dy;
		d = s.getPt1().getDistance(s.getPt2());
		if (d>this.goalLength) return new Coordinate(0,0);
		if (d==0.0) {
			//low probability
			a = alpha*this.goalLength*0.5/s.getPt1().getIniDistance(s.getPt2());
			dx = a*(s.getPt2().getXIni()-s.getPt1().getXIni());
			dy = a*(s.getPt2().getYIni()-s.getPt1().getYIni());
			if      (p==s.getPt1()) return new Coordinate(-dx, -dy);
			else if (p==s.getPt2()) return new Coordinate( dx,  dy);
			else {
				logger.severe("Error");
				return null;
			}
		}
		a = alpha*(this.goalLength-d)/d*0.5;
		dx = a*(s.getPt2().getX()-s.getPt1().getX());
		dy = a*(s.getPt2().getY()-s.getPt1().getY());
		if      (p==s.getPt1()) return new Coordinate(-dx, -dy);
		else if (p==s.getPt2()) return new Coordinate( dx,  dy);
		else {
			logger.severe("Error");
			return null;
		}
	}
}
