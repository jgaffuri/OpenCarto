/**
 * 
 */
package org.opencarto.algo.noding;

import java.util.Collection;
import java.util.HashSet;

import org.opencarto.transfoengine.tesselationGeneralisation.AUnit;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

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
		//improve efficiency !
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


	

	public static Collection<NodingIssue> analyseNoding(LineString l1, LineString l2) {
		//check if points of g1 are noded to points of g2.
		GeometryFactory gf = new GeometryFactory();

		//build spatial index of g1 points
		SpatialIndex segIndex = new STRtree();
		for(Coordinate c : l1.getCoordinates()) segIndex.insert(new Envelope(), c);

		Collection<NodingIssue> out = new HashSet<NodingIssue>();

		//go through segments of g2
		Coordinate[] c2s = l2.getCoordinates();
		Coordinate c1 = c2s[0];
		for(int i = 1; i<c2s.length; i++) {
			//get points close to it with spatial index
			//go through points - check noding of it (in function)
			//compute distance to 2 points. If one is null, continue
			//compute distance to segment. If positive, continue
			//create noding issue
		}
		return out;
	}

	public static NodingIssue analyseNoding(Coordinate c, Coordinate c1, Coordinate c2) {
		//noded case ok
		if( c.distance(c1) == 0 ) return null;
		if( c.distance(c2) == 0 ) return null;
		//not noded case ok
		if( new LineSegment(c1,c2).distance(c) > 0 ) return null;
		return new NodingIssue(c);
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
