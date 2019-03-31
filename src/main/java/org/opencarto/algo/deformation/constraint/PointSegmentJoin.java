package org.opencarto.algo.deformation.constraint;

import java.util.logging.Logger;

import org.locationtech.jts.geom.Coordinate;
import org.opencarto.algo.deformation.base.GPoint;
import org.opencarto.algo.deformation.base.GRelationnalConstraint;
import org.opencarto.algo.deformation.submicro.GSegment;
import org.opencarto.algo.deformation.submicro.GSinglePoint;

public class PointSegmentJoin extends GRelationnalConstraint {
	private static Logger logger = Logger.getLogger(PointSegmentJoin.class.getName());

	private GSinglePoint ps;
	private GSegment s;

	public PointSegmentJoin(GSinglePoint ps, GSegment s, double importance){
		super(ps,s,importance);
		this.ps = ps;
		this.s = s;
	}

	@Override
	public Coordinate getDisplacement(GPoint p, double alpha) {
		double ps1, ps2;
		//calculs des produits scalaires pour connaitre la configuration
		ps1 = (this.s.getPt2().getX()-this.s.getPt1().getX())*(this.ps.getPoint().getX()-this.s.getPt1().getX())+(this.s.getPt2().getY()-this.s.getPt1().getY())*(this.ps.getPoint().getY()-this.s.getPt1().getY());
		ps2 = (this.s.getPt1().getX()-this.s.getPt2().getX())*(this.ps.getPoint().getX()-this.s.getPt2().getX())+(this.s.getPt1().getY()-this.s.getPt2().getY())*(this.ps.getPoint().getY()-this.s.getPt2().getY());

		if (p==this.ps.getPoint()){
			if (ps1>0.0&&ps2>0.0){
				//le point p est entre s.p1 et s.p2
				Coordinate proj = this.s.getProjected(this.ps.getPoint());
				return new Coordinate(alpha*0.5*(proj.x-p.getX()), alpha*0.5*(proj.y-p.getY()));
			}
			//le point p est du cote de s.p1
			else if (ps1<=0.0&&ps2>0.0)
				return new Coordinate(alpha*0.5*(this.s.getPt1().getX()-p.getX()), alpha*0.5*(this.s.getPt1().getY()-p.getY()));
			else if (ps1>0.0&&ps2<=0.0)
				return new Coordinate(alpha*0.5*(this.s.getPt2().getX()-p.getX()), alpha*0.5*(this.s.getPt2().getY()-p.getY()));
		}
		else if (p==this.s.getPt1()||p==this.s.getPt2()){
			if (ps1>0.0&&ps2>0.0){
				//le minimum est atteint au niveau du projete du point sur le segment.
				Coordinate proj = this.s.getProjected(this.ps.getPoint());
				double d = p.getDistance(proj.x, proj.y);
				double dd = this.s.getLength();
				return new Coordinate((dd-d)/dd*alpha*0.5*(this.ps.getPoint().getX()-proj.x), (dd-d)/dd*alpha*0.5*(this.ps.getPoint().getY()-proj.y));
			}	
			return new Coordinate(alpha*0.5*(this.ps.getPoint().getX()-p.getX()), alpha*0.5*(this.ps.getPoint().getY()-p.getY()));
		}
		logger.severe("Error");
		return null;
	}
}
