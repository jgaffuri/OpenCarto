/**
 * 
 */
package org.opencarto.algo.noding;

import java.util.Collection;
import java.util.HashSet;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

/**
 * @author julien Gaffuri
 *
 */
public class NodingUtil {

	public static Collection<NodingIssue> analyseNoding(Geometry g1, Geometry g2) {
		Collection<NodingIssue> out = new HashSet<NodingIssue>();
		out.addAll(a_(g1,g2)); out.addAll(a_(g2,g1));
		return out;
	}
	private static Collection<NodingIssue> a_(Geometry g1, Geometry g2) {
		//check if points of g1 are noded to points of g2.
		GeometryFactory gf = new GeometryFactory();

		Collection<NodingIssue> out = new HashSet<NodingIssue>();
		MultiPoint g2_pt = gf.createMultiPoint(g2.getCoordinates());
		for(Coordinate c : g1.getCoordinates()) {
			Point pt = gf.createPoint(c);
			//correct noded case
			if( pt.distance(g2_pt) == 0 ) continue;
			//correct not noded case
			if( pt.distance(g2) > 0 ) continue;
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
		Coordinate[] csOut = new Coordinate[cs.length+1];
		int indexAdd;
		for(int seg=0; seg<cs.length-1; seg++) {
			Coordinate cs1 = cs[seg], cs2 = cs[seg+1];
			
		}

		return new GeometryFactory().createLineString(csOut);
	}

}
