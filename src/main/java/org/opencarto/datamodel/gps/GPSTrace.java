/**
 * 
 */
package org.opencarto.datamodel.gps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import eu.europa.ec.eurostat.eurogeostat.util.ProjectionUtil;

/**
 * @author JGaffuri
 *
 */
public class GPSTrace extends Route {
	//static Logger logger = Logger.getLogger(GPSTrace.class.getName());

	public GPSTrace(ArrayList<Lap> laps){
		this.laps = laps;

		ArrayList<GPSPoint> points = new ArrayList<GPSPoint>();
		for(Lap lap : getLaps()) points.addAll(lap.getPoints());
		setPoints(points);

		for(GPSPoint pt : getPoints()) pt.setGPSTrace(this);

		//build geometry
		if(getPoints().size() == 0 || getPoints().size() == 1) {
			//logger.warn("GPS trace with 0 or 1 point: " + this);
			return;
		}
		Coordinate[] coords = new Coordinate[getPoints().size()];
		int i=0;
		for(GPSPoint pt : getPoints()) coords[i++] = pt.getCoord();
		setDefaultGeometry( new LineString(new CoordinateArraySequence(coords), new GeometryFactory()) );
	}

	private ArrayList<Lap> laps = null;
	public ArrayList<Lap> getLaps() { return this.laps; }


	@Override
	public void computeLengthM() {
		double sum = 0;
		for(Lap lap : getLaps()) sum += lap.getLengthM();
		setLengthM(sum);
	}


	private boolean pointsDistanceCoordinateComputed = false;
	public void computePointsDistanceCoordinate(){
		if(this.pointsDistanceCoordinateComputed) return;

		int nb = getPoints().size();
		GPSPoint pt_ = getPoints().get(0), pt;
		pt_.setDistanceCoordinate(0);
		for(int i=1; i<nb; i++) {
			pt = getPoints().get(i);
			pt.setDistanceCoordinate(pt_.getDistanceCoordinate() + pt_.getCoord().distance(pt.getCoord())
					* ProjectionUtil.getDeformationFactor( (pt_.getLat()+pt.getLat())*0.5 ));
			pt_=pt;
		}
		this.pointsDistanceCoordinateComputed = true;
	}

	private boolean pointsTimeCoordinateComputed = false;
	public void computePointsTimeCoordinate(){
		if(this.pointsTimeCoordinateComputed) return;

		double startTime = getPoints().get(0).getTime().getDate().getTime();
		for (GPSPoint pt : getPoints())
			pt.setTimeCoordinate( (int) ((pt.getTime().getDate().getTime() - startTime) * 0.001) );
		this.pointsTimeCoordinateComputed = true;
	}


	private ArrayList<GPSSegment> segments;
	public ArrayList<GPSSegment> getSegments() {
		if( this.segments == null ) {
			this.segments = new ArrayList<GPSSegment>();
			int nb = getPoints().size();
			if (nb <= 1) return this.segments;
			GPSPoint startPoint = getPoints().get(0);
			GPSPoint endPoint;
			for(int i=1; i<nb; i++) {
				endPoint = getPoints().get(i);
				this.segments.add( new GPSSegment(startPoint, endPoint) );
				startPoint = endPoint;
			}
		}
		return this.segments;
	}

	private double minSpeed = -1;
	public double getMinSpeedKmH() {
		if(this.minSpeed==-1) computeMinMaxSpeedKmH();
		return this.minSpeed;
	}

	private double maxSpeed = -1;
	public double getMaxSpeedKmH() {
		if(this.maxSpeed==-1) computeMinMaxSpeedKmH();
		return this.maxSpeed;
	}

	private void computeMinMaxSpeedKmH() {
		this.minSpeed = Double.MAX_VALUE;
		this.maxSpeed = 0;
		for(GPSSegment segment : getSegments() ) {
			double speed = segment.getMeanSpeedKmH();
			if(speed<this.minSpeed) this.minSpeed = speed;
			if(speed>this.maxSpeed) this.maxSpeed = speed;
		}
	}


	private double minGPSElevation = -1;
	public double getMinGPSElevation() {
		if(this.minGPSElevation==-1) computeMinMaxGPSElevation();
		return this.minGPSElevation;
	}

	private double maxGPSElevation = -1;
	public double getMaxGPSElevation() {
		if(this.maxGPSElevation==-1) computeMinMaxGPSElevation();
		return this.maxGPSElevation;
	}

	private void computeMinMaxGPSElevation() {
		this.minGPSElevation = Double.MAX_VALUE;
		this.maxGPSElevation = 0;
		for(GPSPoint pt : getPoints() ) {
			double elevation = pt.getGPSElevation();
			if(elevation<this.minGPSElevation) this.minGPSElevation = elevation;
			if(elevation>this.maxGPSElevation) this.maxGPSElevation = elevation;
		}
	}


	private double minGPSElevationSegments = -1;
	public double getMinGPSElevationSegments() {
		if(this.minGPSElevationSegments==-1) computeMinMaxGPSElevationSegments();
		return this.minGPSElevationSegments;
	}

	private double maxGPSElevationSegments = -1;
	public double getMaxGPSElevationSegments() {
		if(this.maxGPSElevationSegments==-1) computeMinMaxGPSElevationSegments();
		return this.maxGPSElevationSegments;
	}

	private void computeMinMaxGPSElevationSegments() {
		this.minGPSElevationSegments = Double.MAX_VALUE;
		this.maxGPSElevationSegments = 0;
		for(GPSSegment segment : getSegments() ) {
			double elevation = segment.getGPSElevation();
			if(elevation<this.minGPSElevationSegments) this.minGPSElevationSegments = elevation;
			if(elevation>this.maxGPSElevationSegments) this.maxGPSElevationSegments = elevation;
		}
	}


	/*
	private double minGMapElevationSegments = -1;
	public double getMinGMapElevationSegments() {
		if(this.minGMapElevationSegments==-1) computeMinMaxGMapElevationSegments();
		return this.minGMapElevationSegments;
	}

	private double maxGMapElevationSegments = -1;
	public double getMaxGMapElevationSegments() {
		if(this.maxGMapElevationSegments==-1) computeMinMaxGMapElevationSegments();
		return this.maxGMapElevationSegments;
	}

	private void computeMinMaxGMapElevationSegments() {
		this.minGMapElevationSegments = Double.MAX_VALUE;
		this.maxGMapElevationSegments = 0;
		for(GPSSegment segment : getSegments() ) {
			double elevation = segment.getGMapElevation();
			if(elevation<this.minGMapElevationSegments) this.minGMapElevationSegments = elevation;
			if(elevation>this.maxGMapElevationSegments) this.maxGMapElevationSegments = elevation;
		}
	}
	 */
	/**
	 * Get the elevation values of the trace's points from GMap
	 */
	/*	public void computeGMapAltitude(){
		GMapElevationWebService.computeGMapAltitude(getPoints());
	}
	 */






	private ArrayList<GPSSegment> distSegSpeed = null;
	public ArrayList<GPSSegment> getDistributionSegmentsSpeed() {
		if(this.distSegSpeed==null) {
			this.distSegSpeed = new ArrayList<GPSSegment>();
			this.distSegSpeed.addAll(getSegments());
			Comparator<GPSSegment> c = new Comparator<GPSSegment>(){
				public int compare(GPSSegment seg1, GPSSegment seg2) {
					double diff = seg1.getMeanSpeedMS()-seg2.getMeanSpeedMS();
					if(diff>0) return 1;
					if(diff<0) return -1;
					return 0;
				}};
				Collections.sort(this.distSegSpeed, c); 
		}
		return this.distSegSpeed;
	}

	private ArrayList<GPSSegment> distEleGPS = null;
	public ArrayList<GPSSegment> getDistributionSegmentsElevationGPS() {
		if(this.distEleGPS==null) {
			this.distEleGPS = new ArrayList<GPSSegment>();
			this.distEleGPS.addAll(getSegments());
			Comparator<GPSSegment> c = new Comparator<GPSSegment>(){
				public int compare(GPSSegment seg1, GPSSegment seg2) {
					double diff = seg1.getGPSElevation()-seg2.getGPSElevation();
					if(diff>0) return 1;
					if(diff<0) return -1;
					return 0;
				}};
				Collections.sort(this.distEleGPS, c); 
		}
		return this.distEleGPS;
	}

	/*
	private ArrayList<GPSSegment> distEleGMap = null;
	public ArrayList<GPSSegment> getDistributionSegmentsElevationGMap() {
		if(this.distEleGMap==null) {
			this.distEleGMap = new ArrayList<GPSSegment>();
			this.distEleGMap.addAll(getSegments());
			Comparator<GPSSegment> c = new Comparator<GPSSegment>(){
				@Override
				public int compare(GPSSegment seg1, GPSSegment seg2) {
					double diff = seg1.getGMapElevation()-seg2.getGMapElevation();
					if(diff>0) return 1;
					if(diff<0) return -1;
					return 0;
				}};
				Collections.sort(this.distEleGMap, c); 
		}
		return this.distEleGMap;
	}
	 */






	public String toKML(String styleId, int tabNb) {
		String tab = "";
		for(int i=0; i<tabNb; i++) tab += "\t";

		StringBuffer sb = new StringBuffer()
		.append(tab + "<Placemark>\n")

		//name and style
		.append(tab + "\t<name>Trace - " + getStartTime() + "</name>\n");
		if(styleId != null && !styleId.isEmpty() && !"".equals(styleId))
			sb.append(tab + "\t<styleUrl>#" + styleId + "</styleUrl>\n");

		//geometry
		sb.append(tab + "\t<LineString>\n")
		.append(tab + "\t\t<coordinates>\n");

		//coordinates
		for(GPSPoint pt : getPoints()) {
			sb.append(tab + "\t\t\t")
			.append(pt.getLon()).append(",").append(pt.getLat()).append(",").append(pt.getGPSElevation())
			.append("\n");
		}

		sb.append(tab + "\t\t</coordinates>\n")
		.append(tab + "\t</LineString>\n")
		.append(tab + "</Placemark>\n");

		return sb.toString();
	}


	public BufferedImage getElevationProfileInDistance(int wt, int ht) {
		BufferedImage bi = new BufferedImage(wt, ht, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bi.createGraphics();

		g2.setColor(new Color(0, 0, 0, 0));
		g2.fillRect(0, 0, wt, ht);

		//g2.setColor(Color.BLACK);
		//g2.drawRect(0, 0, wt-1, ht-1);

		g2.setColor(Color.BLUE);

		double min = getMinGPSElevation();
		double max = getMaxGPSElevation();
		double len = getLengthM();

		for(GPSSegment seg : getSegments()){
			double width = wt*seg.getLengthM()/len;
			double height = ht*(seg.getGPSElevation()-min)/(max-min);
			double x = wt*seg.getStartPoint().getDistanceCoordinate()/len;
			double y = ht-height;
			g2.drawRect((int)x, (int)y, (int)width, (int)height);
			g2.fillRect((int)x, (int)y, (int)width, (int)height);
		}
		return bi;
	}

	public BufferedImage getSpeedProfileInDistance(int wt, int ht) {
		BufferedImage bi = new BufferedImage(wt, ht, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bi.createGraphics();

		g2.setColor(new Color(0, 0, 0, 0));
		g2.fillRect(0, 0, wt, ht);

		//g2.setColor(Color.BLACK);
		//g2.drawRect(0, 0, wt-1, ht-1);

		g2.setColor(Color.BLUE);

		double min = getMinSpeedKmH();
		double max = getMaxSpeedKmH();
		double len = getLengthM();

		for(GPSSegment seg : getSegments()){
			double width = wt*seg.getLengthM()/len;
			double height = ht*(seg.getMeanSpeedKmH()-min)/(max-min);
			double x = wt*seg.getStartPoint().getDistanceCoordinate()/len;
			double y = ht-height;
			g2.drawRect((int)x, (int)y, (int)width, (int)height);
			g2.fillRect((int)x, (int)y, (int)width, (int)height);
		}
		return bi;
	}

}
