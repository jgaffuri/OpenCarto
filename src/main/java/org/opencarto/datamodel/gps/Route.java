/**
 * 
 */
package org.opencarto.datamodel.gps;

import java.util.ArrayList;

import org.opencarto.datamodel.Feature;


/**
 * @author julien Gaffuri
 *
 */
public abstract class Route extends Feature implements Comparable<Route> {
	//private static Logger logger = Logger.getLogger(Route.class.getName());

	private ArrayList<GPSPoint> points;
	public ArrayList<GPSPoint> getPoints() { return this.points; }
	public void setPoints(ArrayList<GPSPoint> points) { this.points = points; }

	public GPSPoint getStartPoint() { return this.getPoints().get(0); }
	public GPSPoint getEndPoint() { return this.getPoints().get(getPoints().size()-1); }

	public GPSTime getStartTime() {
		return getStartPoint().getTime();
	}

	public GPSTime getEndTime() {
		return getEndPoint().getTime();
	}

	private double durationS = -999;
	public double getDurationS() {
		if(this.durationS == -999) { computeDurationS(); }
		return this.durationS;
	}
	public void setDurationS(double durationS) { this.durationS = durationS; }
	public void computeDurationS() {
		if(getStartTime() != null && getEndTime() != null)
			setDurationS( GPSTime.getDurationS(getStartTime(), getEndTime()) );
	}


	private double lengthM = -999;
	public double getLengthM() {
		if(this.lengthM == -999) { computeLengthM(); }
		return this.lengthM;
	}
	public void setLengthM(double lengthM) { this.lengthM = lengthM; }
	public abstract void computeLengthM();


	public double getMeanSpeedMS() {
		return getLengthM() / getDurationS();
	}

	public double getMeanSpeedKmH() {
		return 3.6 * getMeanSpeedMS();
	}


	@Override
	public int compareTo(Route route) {
		if(getStartTime() == null) return 1;
		if(route.getStartTime() == null) return -1;
		return getStartTime().compareTo(route.getStartTime());
	}

	@Override
	public String toString() {
		if(this.getStartTime() == null) return "No time";
		return getStartTime().toString();
	}

}
