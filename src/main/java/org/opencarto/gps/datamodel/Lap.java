/**
 * 
 */
package org.opencarto.gps.datamodel;

import java.util.ArrayList;

import eu.europa.ec.eurostat.jgiscotools.util.ProjectionUtil;

/**
 * @author julien Gaffuri
 *
 */
public class Lap extends Route {
	//private static Logger logger = Logger.getLogger(Lap.class.getName());

	public Lap(ArrayList<GPSPoint> points) {
		setPoints(points);
	}

	@Override
	public void computeLengthM() {
		if(getPoints() == null || getPoints().size()==0) {
			setLengthM(0);
			return;
		}

		double total = 0;
		GPSPoint pt_, pt = getPoints().get(0);
		for(int i = 1; i<getPoints().size(); i++) {
			pt_ = getPoints().get(i);
			total += pt_.getCoord().distance(pt.getCoord()) * ProjectionUtil.getDeformationFactor( (pt_.getLat()+pt.getLat())*0.5 );			
			pt = pt_;
		}
		setLengthM(total);
	}
}
