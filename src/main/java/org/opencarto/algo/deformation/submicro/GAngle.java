package org.opencarto.algo.deformation.submicro;

import org.opencarto.algo.deformation.base.GPoint;
import org.opencarto.algo.deformation.base.Submicro;

public class GAngle extends Submicro {

	private GPoint pt;
	public GPoint getPt() { return this.pt; }
	private GPoint pt1;
	public GPoint getPt1() { return this.pt1; }
	private GPoint pt2;
	public GPoint getPt2() { return this.pt2; }

	public GAngle(GPoint pt1, GPoint pt, GPoint pt2){
		getPoints().add(pt1);
		getPoints().add(pt);
		getPoints().add(pt2);

		this.pt = pt;
		//angle (p1,p,p2) has to be direct direct. computes the vp.
		if ((pt1.getXIni()-pt.getXIni())*(pt2.getYIni()-pt.getYIni())-(pt1.getYIni()-pt.getYIni())*(pt2.getXIni()-pt.getXIni()) > 0) {this.pt1=pt1; this.pt2=pt2;}
		else {this.pt1=pt2; this.pt2=pt1;}

		pt1.getPointsRel().add(pt2);
		pt1.getPointsRel().add(pt);
		pt2.getPointsRel().add(pt1);
		pt2.getPointsRel().add(pt);
		pt.getPointsRel().add(pt1);
		pt.getPointsRel().add(pt2);
	}


	//within [0,Pi]
	public double getValue() {
		double value = Math.atan2( (getPt1().getX()-getPt().getX())*(getPt2().getY()-getPt().getY())-(getPt1().getY()-getPt().getY())*(getPt2().getX()-getPt().getX()) ,
				(getPt1().getX()-getPt().getX())*(getPt2().getX()-getPt().getX())+(getPt1().getY()-getPt().getY())*(getPt2().getY()-getPt().getY()) );
		if ( value < 0.0 ) value += 2*Math.PI;
		return value;
	}

	//within [0,Pi]
	private double initialValue = -999.9;
	public double getInitialValue() {
		if (this.initialValue == -999.9) {
			//vp
			double pv = (getPt1().getXIni()-getPt().getXIni())*(getPt2().getYIni()-getPt().getYIni())-(getPt1().getYIni()-getPt().getYIni())*(getPt2().getXIni()-getPt().getXIni());
			//sp
			double ps = (getPt1().getXIni()-getPt().getXIni())*(getPt2().getXIni()-getPt().getXIni())+(getPt1().getYIni()-getPt().getYIni())*(getPt2().getYIni()-getPt().getYIni());
			this.initialValue = Math.atan2(pv, ps);
		}
		return this.initialValue;
	}

	//within [-Pi,Pi]
	public double getValueDifference(double val) {
		double diff = val - getValue();
		if      (diff<-Math.PI) return diff+2*Math.PI;
		else if (diff> Math.PI) return diff-2*Math.PI;
		else return diff;
	}

	//within [-Pi,Pi]
	public double getValueDifference() {
		return getValueDifference(getInitialValue());
	}


	@Override
	public double getX(){
		return getPt().getX();
	}
	@Override
	public double getY(){
		return getPt().getY();
	}

	@Override
	public void clean(){
		super.clean();
		this.pt = null;
		this.pt1 = null;
		this.pt2 = null;
	}
}
