package org.opencarto.algo.deformation.constraint;

import org.opencarto.algo.deformation.base.GPoint;
import org.opencarto.algo.deformation.base.GSimpleConstraint;
import org.opencarto.algo.deformation.submicro.GSinglePoint;

import com.vividsolutions.jts.geom.Coordinate;

public class PointPosition extends GSimpleConstraint {

	private Coordinate goalC;

	public PointPosition(GSinglePoint p, double imp, Coordinate goalC){
		super(p, imp);
		this.goalC = goalC;
	}

	public PointPosition(GSinglePoint p, double importance){
		this(p, importance, p.getPoint().getInitialPosition());
	}

	@Override
	public Coordinate getDisplacement(GPoint p, double alpha) {
		return new Coordinate(alpha*(this.goalC.x-p.getX()), alpha*(this.goalC.y-p.getY()));
	}
}
