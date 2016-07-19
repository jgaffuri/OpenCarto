package org.opencarto.algo.deformation.constraint;

import org.opencarto.algo.deformation.base.GPoint;
import org.opencarto.algo.deformation.base.GSimpleConstraint;
import org.opencarto.algo.deformation.base.Submicro;

import com.vividsolutions.jts.geom.Coordinate;

public class SubmicroCenterPosition extends GSimpleConstraint {

	private Submicro sm;
	private Coordinate goalC;

	public SubmicroCenterPosition(Submicro sm, double imp, Coordinate goalC) {
		super(sm, imp);
		this.sm = sm;
		this.goalC = goalC;
	}

	@Override
	public Coordinate getDisplacement(GPoint pt, double alpha) {
		return new Coordinate(alpha*(this.goalC.x-sm.getX()), alpha*(this.goalC.y-sm.getY()));
	}
}
