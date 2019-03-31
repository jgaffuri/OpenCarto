/**
 * 
 */
package org.opencarto.datamodel.gps;

import org.locationtech.jts.geom.Coordinate;
import org.opencarto.util.ProjectionUtil;


/**
 * @author julien Gaffuri
 *
 */
public class GPSPoint {

	private GPSTrace trace;
	public void setGPSTrace(GPSTrace trace) { this.trace = trace; }

	public GPSPoint(double lat, double lon, double elevation, String timeStamp){
		this.lat = lat;
		this.lon = lon;
		this.GPSelevation = elevation;
		if(timeStamp != null && !"".equals(timeStamp)) this.time = new GPSTime(timeStamp);
	}

	private double lat = -9999999;
	public synchronized double getLat(){ return this.lat; }

	private double lon = -9999999;
	public synchronized double getLon(){ return this.lon; }

	private double GPSelevation = -9999999;
	public synchronized double getGPSElevation(){ return this.GPSelevation; }

	private GPSTime time;
	public synchronized GPSTime getTime(){ return this.time; }

	private Coordinate coord;
	public synchronized Coordinate getCoord(){
		if(this.coord == null) computeCoord();
		return this.coord;
	}

	public synchronized void computeCoord() {
		this.coord = new Coordinate();
		getCoord().x = ProjectionUtil.getXGeo(getLon());
		getCoord().y = ProjectionUtil.getYGeo(getLat());
	}




	private GPSSegment segmentIn;
	public GPSSegment getSegmentIn() { return this.segmentIn; }
	public void setSegmentIn(GPSSegment segment) { this.segmentIn = segment; }

	private GPSSegment segmentOut;
	public GPSSegment getSegmentOut() { return this.segmentOut; }
	public void setSegmentOut(GPSSegment segment) { this.segmentOut = segment; }


/*
	private double gMapElevation = -9999999;
	public void setGMapElevation(double gMapAltitude) { this.gMapElevation = gMapAltitude; }
	public synchronized double getGMapElevation(){
		if( this.gMapElevation == -9999999 ) this.trace.computeGMapAltitude();
		return this.gMapElevation;
	}
*/

	private double speedMS = -1;
	public double getSpeedMS() {
		if( this.speedMS == -1 ){
			double speedIn  = getSegmentIn()  == null? 0 : getSegmentIn().getMeanSpeedMS();
			double tIn = getSegmentIn()  == null? 0 : getSegmentIn().getDurationS();
			double speedOut = getSegmentOut() == null? 0 : getSegmentOut().getMeanSpeedMS();
			double tOut = getSegmentOut() == null? 0 : getSegmentOut().getDurationS();

			if ( tIn + tOut == 0 ) this.speedMS = 0;
			else this.speedMS = ( tIn * speedIn + tOut * speedOut ) / ( tIn + tOut );
		}
		return this.speedMS;
	}

	public double getSpeedKmH() {
		return 3.6 * getSpeedMS();
	}

	private double elevationSpeedMH = -9999999.999;
	public double getElevationSpeedMH() {
		if ( this.elevationSpeedMH == -9999999.999 ) {
			double speedIn  = getSegmentIn()  == null? 0 : getSegmentIn().getGPSElevationSpeedMH();
			double tIn = getSegmentIn()  == null? 0 : getSegmentIn().getDurationS();
			double speedOut = getSegmentOut() == null? 0 : getSegmentOut().getGPSElevationSpeedMH();
			double tOut = getSegmentOut() == null? 0 : getSegmentOut().getDurationS();

			if ( tIn + tOut == 0 ) this.elevationSpeedMH = 0;
			else this.elevationSpeedMH = ( tIn * speedIn + tOut * speedOut ) / ( tIn + tOut );
		}
		return this.elevationSpeedMH;
	}

	private int timeCoordinate = -1;
	public synchronized int getTimeCoordinate(){
		if (this.timeCoordinate == -1) this.trace.computePointsTimeCoordinate();
		return this.timeCoordinate;
	}
	public synchronized void setTimeCoordinate(int timeCoordinate){ this.timeCoordinate = timeCoordinate; }

	private double distanceCoordinate = -1;
	public synchronized double getDistanceCoordinate(){
		if (this.distanceCoordinate == -1) this.trace.computePointsDistanceCoordinate();
		return this.distanceCoordinate;
	}
	public synchronized void setDistanceCoordinate(double distanceCoordinate){ this.distanceCoordinate = distanceCoordinate; }

	public String toKML(String styleId, String name, int tabNb) {
		String tab = "";
		for(int i=0; i<tabNb; i++) tab += "\t";

		StringBuffer sb = new StringBuffer()

		.append(tab + "<Placemark>\n");

		//name and style
		if(name != null && !name.isEmpty() && !"".equals(name))
			sb.append(tab + "\t<name>" + name + "</name>\n");
		if(styleId != null && !styleId.isEmpty() && !"".equals(styleId))
			sb.append(tab + "\t<styleUrl>#" + styleId + "</styleUrl>\n");

		//geometry
		sb.append(tab + "\t<Point><coordinates>")
		.append(getLon()).append(",").append(getLat())
		.append("</coordinates></Point>\n")

		.append(tab + "</Placemark>\n");

		return sb.toString();
	}
}
