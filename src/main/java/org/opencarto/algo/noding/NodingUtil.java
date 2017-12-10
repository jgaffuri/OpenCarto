/**
 * 
 */
package org.opencarto.algo.noding;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * @author julien Gaffuri
 *
 */
public class NodingUtil {

	//check if points of mp1 are noded to points of mp2.
	public static Collection<NodingIssue> analyseNoding(MultiPolygon mp1, MultiPolygon mp2) {

		//build spatial index of mp1 polygons
		SpatialIndex index = new STRtree();
		for(int i=0; i<mp1.getNumGeometries(); i++) {
			Polygon p1 = (Polygon) mp1.getGeometryN(i);
			index.insert(p1.getEnvelopeInternal(), p1);
		}

		Collection<NodingIssue> out = new HashSet<NodingIssue>();

		//go through polygons of mp2
		for(int i=0; i<mp2.getNumGeometries(); i++) {
			Polygon p2 = (Polygon) mp2.getGeometryN(i);

			//get p1s close to p2 and check noding of it
			for(Polygon p1 : (List<Polygon>)index.query(p2.getEnvelopeInternal()))
				out.addAll( analyseNoding(p1,p2) );
		}
		return out;
	}

	//check if points of p1 are noded to points of p2.
	public static Collection<NodingIssue> analyseNoding(Polygon p1, Polygon p2) {

		//build spatial index of p1 rings
		SpatialIndex index = new STRtree();
		index.insert(p1.getExteriorRing().getEnvelopeInternal(), p1.getExteriorRing());
		for(int i=0; i<p1.getNumInteriorRing(); i++) {
			LinearRing lr1 = (LinearRing) p1.getInteriorRingN(i);
			index.insert(lr1.getEnvelopeInternal(), lr1);
		}
		
		Collection<NodingIssue> out = new HashSet<NodingIssue>();

		//build collection of mp2 rings
		Collection<LinearRing> lr2s = new HashSet<LinearRing>();
		lr2s.add((LinearRing) p2.getExteriorRing());
		for(int i=0; i<p2.getNumInteriorRing(); i++)
			lr2s.add((LinearRing) p2.getInteriorRingN(i));
		

		//go through rings of mp2
		for(LinearRing lr2 : lr2s) {
			//get lr1s close to lr2 and check noding of it
			for(LinearRing lr1 : (List<LinearRing>)index.query(lr2.getEnvelopeInternal()))
				out.addAll( analyseNoding(lr1,lr2) );
		}
		return out;
	}


	//check if points of l1 are noded to points of l2.
	public static Collection<NodingIssue> analyseNoding(LineString l1, LineString l2) {

		//build spatial index of l1 points
		SpatialIndex index = new STRtree();
		for(Coordinate c : l1.getCoordinates()) index.insert(new Envelope(c), c);

		Collection<NodingIssue> out = new HashSet<NodingIssue>();

		//go through segments of l2
		Coordinate[] c2s = l2.getCoordinates();
		Coordinate c1 = c2s[0], c2;
		for(int i=1; i<c2s.length; i++) {
			c2 = c2s[i];

			//get points close to segment and check noding of it
			for(Coordinate c : (List<Coordinate>)index.query(new Envelope(c1,c2))) {
				NodingIssue ni = analyseNoding(c,c1,c2);
				if(ni != null) out.add(ni);
			}
			c1 = c2;
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








	/*public static Collection<NodingIssue> analyseNodingBoth(Geometry g1, Geometry g2) {
		Collection<NodingIssue> out = new HashSet<NodingIssue>();
		out.addAll(analyseNoding(g1,g2)); out.addAll(analyseNoding(g2,g1));
		return out;
	}*/

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







	/*/fix a noding issue by including a coordinate (which is supposed to be noded) into the geometry representation
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
	}*/

}