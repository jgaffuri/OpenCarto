/**
 * 
 */
package org.opencarto.algo.noding;

import java.util.Collection;
import java.util.HashSet;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

/**
 * @author julien Gaffuri
 *
 */
public class NodingUtil {

	public static Collection<NodingIssue> analyseNodingBoth(Geometry g1, Geometry g2) {
		Collection<NodingIssue> out = new HashSet<NodingIssue>();
		out.addAll(analyseNoding(g1,g2)); out.addAll(analyseNoding(g2,g1));
		return out;
	}
	public static Collection<NodingIssue> analyseNoding(Geometry g1, Geometry g2) {
		//check if points of g1 are noded to points of g2.
		GeometryFactory gf = new GeometryFactory();

		Collection<NodingIssue> out = new HashSet<NodingIssue>();
		MultiPoint g2_pt = gf.createMultiPoint(g2.getCoordinates());
		for(Coordinate c : g1.getCoordinates()) {
			Point pt = gf.createPoint(c);
			//noded case ok
			if( pt.distance(g2_pt) == 0 ) continue;
			//not noded case ok
			if( pt.distance(g2) > 0 ) continue;
			//issue detected
			//System.out.println(c);
			out.add( new NodingIssue(c) );
		}

		return out;
	}

	public static class NodingIssue{
		public Coordinate c;
		//public double distance;
		public NodingIssue(Coordinate c) { this.c=c; }
	}


	//fix a noding issue by including a coordinate (which is supposed to be noded) into the geometry representation
	public static LineString fixNodingIssue(LineString ls, Coordinate c) {
		Coordinate[] cs = ls.getCoordinates();
		//get segment index
		int indexAdd;
		for(indexAdd=0; indexAdd<cs.length-1; indexAdd++)
sdghj			if( new LineSegment(cs[indexAdd], cs[indexAdd+1]).distance(c) == 0 ) break;
		//build new line
		Coordinate[] csOut = new Coordinate[cs.length+1];
		csOut[0] = cs[0];
		for(int i=0; i<cs.length+1; i++) {
			if(indexAdd > i) csOut[i] = cs[i];
			else if(indexAdd == i) csOut[i] = c;
			else csOut[i] = cs[i-1];
		}
		return new GeometryFactory().createLineString(csOut);
	}

}
