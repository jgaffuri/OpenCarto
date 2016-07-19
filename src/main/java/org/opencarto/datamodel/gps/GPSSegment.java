/**
 * 
 */
package org.opencarto.datamodel.gps;

import java.util.ArrayList;

import org.opencarto.util.ProjectionUtil;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * @author julien Gaffuri
 *
 */
public class GPSSegment extends Route {

	public GPSSegment(GPSPoint startPoint, GPSPoint endPoint) {
		setPoints(new ArrayList<GPSPoint>());
		getPoints().add(startPoint);
		startPoint.setSegmentOut(this);
		getPoints().add(endPoint);
		startPoint.setSegmentIn(this);
	}

	private LineString geometry = null;
	public LineString getGeometry() {
		if ( this.geometry == null )
			this.geometry = new GeometryFactory().createLineString( new Coordinate[] { getStartPoint().getCoord(), getEndPoint().getCoord() } );
		return this.geometry;
	}

	@Override
	public void computeLengthM() {
		double lengthM = getStartPoint().getCoord().distance(getEndPoint().getCoord())
			* ProjectionUtil.getDeformationFactor( (getStartPoint().getLat()+getEndPoint().getLat())*0.5 );		
		setLengthM(lengthM);
	}



	/**
	 * @return the elevation variation of the segment (difference of elevation between its end and start point), in meters
	 */
	public double getGPSElevationVariation() {
		return getEndPoint().getGPSElevation() - getStartPoint().getGPSElevation();
	}

	public double getGPSElevation() {
		return (getEndPoint().getGPSElevation() + getStartPoint().getGPSElevation() )*0.5;
	}

	public double getGPSElevationSpeedMH() {
		return 3600 * getGPSElevationVariation() / getDurationS();
	}
/*
	public double getGMapElevationVariation() {
		return getEndPoint().getGMapElevation() - getStartPoint().getGMapElevation();
	}

	public double getGMapElevation() {
		return (getEndPoint().getGMapElevation() + getStartPoint().getGMapElevation() )*0.5;
	}

	public double getGMapElevationSpeedMH() {
		return 3600 * getGMapElevationVariation() / getDurationS();
	}
*/
	public Coordinate getCenter() {
		return new Coordinate( ( getStartPoint().getCoord().x + getEndPoint().getCoord().x )*0.5 , ( getStartPoint().getCoord().y + getEndPoint().getCoord().y )*0.5 );
	}

	public String toKML(String styleId, String name, int tabNb) {
		String tab = "";
		for(int i=0; i<tabNb; i++) tab += "\t";

		StringBuffer sb = new StringBuffer();

		sb.append(tab + "<Placemark>\n");

		//name and style
		if(name != null && !name.isEmpty() && !"".equals(name))
			sb.append(tab + "\t<name>" + name + "</name>\n");
		if(styleId != null && !styleId.isEmpty() && !"".equals(styleId))
			sb.append(tab + "\t<styleUrl>#" + styleId + "</styleUrl>\n");

		//the geometry
		sb.append(tab + "\t<LineString>\n")
		.append(tab + "\t\t<coordinates>\n")

		//start point
		.append(tab + "\t\t\t")
		.append(getStartPoint().getLon()).append(",").append(getStartPoint().getLat()).append(",").append(getStartPoint().getGPSElevation())
		.append("\n")

		//end point
		.append(tab + "\t\t\t")
		.append(getEndPoint().getLon()).append(",").append(getEndPoint().getLat()).append(",").append(getEndPoint().getGPSElevation())
		.append("\n")

		.append(tab + "\t\t</coordinates>\n")
		.append(tab + "\t</LineString>\n")
		.append(tab + "</Placemark>\n");

		return sb.toString();
	}

	public String centerPointToKML(String styleId, String name, int tabNb) {
		String tab = "";
		for(int i=0; i<tabNb; i++) tab += "\t";

		StringBuffer sb = new StringBuffer()
		.append(tab + "<Placemark>\n");

		//name and style
		if(name != null && !name.isEmpty() && !"".equals(name))
			sb.append(tab + "\t<name>" + name + "</name>\n");
		if(styleId != null && !styleId.isEmpty() && !"".equals(styleId))
			sb.append(tab + "\t<styleUrl>#" + styleId + "</styleUrl>\n");

		//center point
		double lon = (getStartPoint().getLon()+getEndPoint().getLon())*0.5;
		double lat = (getStartPoint().getLat()+getEndPoint().getLat())*0.5;
		sb.append(tab + "\t<Point><coordinates>")
		.append(lon).append(",").append(lat)
		.append("</coordinates></Point>\n")

		.append(tab + "</Placemark>\n");

		return sb.toString();
	}
}
