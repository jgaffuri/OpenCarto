/**
 * 
 */
package org.opencarto.algo.noding;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * @author julien Gaffuri
 *
 */
public class NodingUtil {
	public final static Logger LOGGER = Logger.getLogger(NodingUtil.class.getName());


	public enum NodingIssueType { PointPoint, LinePoint, Both }

	public static class NodingIssue{
		public NodingIssueType type;
		public Coordinate c;
		public double distance;
		public NodingIssue(NodingIssueType type, Coordinate c, double distance) { this.type=type; this.c=c; this.distance=distance; }
		public String toString() { return "NodingIssue "+type+" c="+c+" d="+distance; }
	}



	//get noding issues for multi-polygonal features
	public static Collection<NodingIssue> getNodingIssues(Collection<Feature> mpfs, double nodingResolution) {
		Collection<NodingIssue> nis = new HashSet<NodingIssue>();
		for(Feature mpf : mpfs) {
			LOGGER.trace(mpf.id);
			nis.addAll(getNodingIssues(NodingIssueType.PointPoint, mpf, mpfs, nodingResolution));
			nis.addAll(getNodingIssues(NodingIssueType.LinePoint, mpf, mpfs, nodingResolution));
		}
		return nis;
	}

	public static Collection<NodingIssue> getNodingIssues(NodingIssueType type, Collection<Feature> mpfs, double nodingResolution) {
		Collection<NodingIssue> nis = new HashSet<NodingIssue>();
		for(Feature mpf : mpfs)
			nis.addAll(getNodingIssues(type, mpf, mpfs, nodingResolution));
		return nis;
	}

	public static Collection<NodingIssue> getNodingIssues(Feature mpf, Collection<Feature> mpfs, double nodingResolution) {
		Collection<NodingIssue> nis = new HashSet<NodingIssue>();
		nis.addAll(getNodingIssues(NodingIssueType.PointPoint, mpf, mpfs, nodingResolution));
		nis.addAll(getNodingIssues(NodingIssueType.LinePoint, mpf, mpfs, nodingResolution));
		return nis;
	}

	public static Collection<NodingIssue> getNodingIssues(NodingIssueType type, Feature mpf, Collection<Feature> mpfs, double nodingResolution) {
		STRtree index = Feature.getSTRtree(mpfs);
		Collection<NodingIssue> nis = new HashSet<NodingIssue>();

		MultiPolygon mp = (MultiPolygon) mpf.getGeom();
		for(Feature au : (List<Feature>) index.query(mp.getEnvelopeInternal())) {
			if(au == mpf) continue;
			if( ! mp.getEnvelopeInternal().intersects(au.getGeom().getEnvelopeInternal()) ) continue;
			Collection<NodingIssue> nis_ = getNodingIssues(type, mp, (MultiPolygon)au.getGeom(), nodingResolution);
			nis.addAll(nis_);
		}
		return nis;
	}


	//check if 1 has noding issues regarding points of 2
	public static Collection<NodingIssue> getNodingIssues(NodingIssueType type, MultiPolygon mp1, MultiPolygon mp2, double nodingResolution) {

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

			//get polygons of mp1 close to p2 and check noding of it
			for(Polygon p1 : (List<Polygon>)index.query(p2.getEnvelopeInternal()))
				out.addAll( getNodingIssues(type,p1,p2,nodingResolution) );
		}
		return out;
	}

	//check if 1 has noding issues regarding points of 2
	public static Collection<NodingIssue> getNodingIssues(NodingIssueType type, Polygon p1, Polygon p2, double nodingResolution) {

		//build spatial index of p1 rings
		SpatialIndex index = new STRtree();
		for(LineString lr1 : JTSGeomUtil.getRings(p1))
			index.insert(lr1.getEnvelopeInternal(), lr1);

		Collection<NodingIssue> out = new HashSet<NodingIssue>();

		//go through rings of mp2
		for(LineString lr2 : JTSGeomUtil.getRings(p2)) {
			//get lr1s close to lr2 and check noding of it
			for(LineString lr1 : (List<LineString>)index.query(lr2.getEnvelopeInternal()))
				out.addAll( getNodingIssues(type,lr1,lr2,nodingResolution) );
		}
		return out;
	}



	//check if 1 has noding issues regarding points of 2
	//check if segments of 1 are fragmented enough to snap to points of 2
	//OR check if coordinates of 1 are not wrongly snapped to coordinates of 2
	public static Collection<NodingIssue> getNodingIssues(NodingIssueType type, LineString l1, LineString l2, double nodingResolution) {

		//build spatial index of l2 points
		SpatialIndex index = new STRtree();
		Coordinate[] c2s = l2.getCoordinates();
		for(int i=0; i<c2s.length+(l2.isClosed()?-1:0); i++) index.insert(new Envelope(c2s[i]), c2s[i]);
		c2s = null;

		Collection<NodingIssue> out = new HashSet<NodingIssue>();

		if(type == NodingIssueType.LinePoint) {
			//go through segments of l1
			Coordinate[] c1s = l1.getCoordinates();
			Coordinate c1 = c1s[0], c2;
			for(int i=1; i<c1s.length; i++) {
				c2 = c1s[i];

				//get points close to segment and check noding of it
				for(Coordinate c : (List<Coordinate>)index.query(new Envelope(c1,c2))) {
					NodingIssue ni = getLinePointNodingIssues(c,c1,c2,nodingResolution);
					if(ni != null) out.add(ni);
				}
				c1 = c2;
			}
		} else if(type == NodingIssueType.PointPoint) {
			//go through coordinates of l1
			Coordinate[] c1s = l1.getCoordinates();
			for(int i=0; i<c1s.length+(l1.isClosed()?-1:0); i++) {
				Coordinate c_ = c1s[i];
				//get points close to it and check noding
				Envelope env = new Envelope(c_); env.expandBy(nodingResolution*1.01);
				for(Coordinate c : (List<Coordinate>)index.query(env)) {
					NodingIssue ni = getPointPointNodingIssues(c,c_,nodingResolution);
					if(ni != null) out.add(ni);
				}
			}
		}
		return out;
	}


	public static NodingIssue getLinePointNodingIssues(Coordinate c, Coordinate c1, Coordinate c2, double nodingResolution) {
		if( checkLPNodingIssue(c,c1,c2,nodingResolution) )
			return new NodingIssue(NodingIssueType.LinePoint,c,new LineSegment(c1,c2).distance(c));
		else
			return null;
	}

	public static boolean checkLPNodingIssue(Coordinate c, Coordinate c1, Coordinate c2, double nodingResolution) {
		return
				c.distance(c1) > nodingResolution
				&& c.distance(c2) > nodingResolution
				&& new LineSegment(c1,c2).distance(c) <= nodingResolution
				;
	}


	public static NodingIssue getPointPointNodingIssues(Coordinate c, Coordinate c_, double nodingResolution) {
		if( checkPPNodingIssue(c,c_,nodingResolution) )
			return new NodingIssue(NodingIssueType.PointPoint,c,c.distance(c_));
		else
			return null;
	}

	public static boolean checkPPNodingIssue(Coordinate c, Coordinate c_, double nodingResolution) {
		double d = c.distance(c_);
		return(d!=0 && d <= nodingResolution);
	}






	/*public static Collection<NodingIssue> getNodingIssuesBoth(Geometry g1, Geometry g2) {
		Collection<NodingIssue> out = new HashSet<NodingIssue>();
		out.addAll(getNodingIssues(g1,g2)); out.addAll(getNodingIssues(g2,g1));
		return out;
	}*/

	/*/generic but highly inefficient method, especially for large geometries
	public static Collection<NodingIssue> getLinePointNodingIssues(Geometry g1, Geometry g2, double nodingResolution) {
		Collection<NodingIssue> out = new HashSet<NodingIssue>();

		//check if points of g1 are noded to points of g2.
		GeometryFactory gf = new GeometryFactory();
		MultiPoint g2_pt = gf.createMultiPoint(g2.getCoordinates());
		Point pt; double d;
		for(Coordinate c : g1.getCoordinates()) {
			pt = gf.createPoint(c);
			//noded case ok
			if( pt.distance(g2_pt) <= nodingResolution ) continue;
			//not noded case ok
			d = pt.distance(g2);
			if( d > nodingResolution ) continue;
			//noding issue detected
			out.add( new NodingIssue(NodingIssueType.LinePoint,c,d) );
		}
		return out;
	}*/






	public static void fixNoding(Collection<Feature> mpfs, double nodingResolution) { fixNoding(NodingIssueType.Both, mpfs, nodingResolution); }
	public static void fixNoding(NodingIssueType type, Collection<Feature> mpfs, double nodingResolution) {
		for(Feature mpf : mpfs)
			fixNoding(type, mpf, mpfs, nodingResolution);
	}


	public static void fixNoding(NodingIssueType type, Feature mpf, Collection<Feature> mpfs, double nodingResolution) {
		STRtree index = Feature.getSTRtree(mpfs);
		MultiPolygon mp = fixNoding(type, (MultiPolygon) mpf.getGeom(), index, nodingResolution);
		mpf.setGeom(mp);

		/*if(LOGGER.isTraceEnabled()) LOGGER.trace("fixNoding-"+type+" for "+mpf.id);
		Collection<NodingIssue> nis = NodingUtil.getNodingIssues(type, mpf, index, nodingResolution);
		int _nb;
		while(nis.size() > 0) {
			if(LOGGER.isTraceEnabled()) LOGGER.trace(mpf.id+" - "+nis.size());
			Coordinate c = nis.iterator().next().c;
			if(LOGGER.isTraceEnabled()) LOGGER.trace(c);
			MultiPolygon mp = fixNoding(type, (MultiPolygon) mpf.getGeom(), c, nodingResolution);
			mpf.setGeom(mp);
			_nb = nis.size();
			nis = NodingUtil.getNodingIssues(type, mpf, index, nodingResolution);
			if(_nb==1 && nis.size()==1) break; //TODO fix this case, when only one issue is left but cannot be solved
		}*/
	}



	public static MultiPolygon fixNoding(NodingIssueType type, MultiPolygon mp, SpatialIndex index, double nodingResolution) {
		Polygon[] ps = new Polygon[mp.getNumGeometries()];
		for(int i=0; i<mp.getNumGeometries(); i++)
			ps[i] = fixNoding(type, (Polygon) mp.getGeometryN(i), index, nodingResolution);
		return mp.getFactory().createMultiPolygon(ps);
	}


	public static Polygon fixNoding(NodingIssueType type, Polygon p, SpatialIndex index, double nodingResolution) {
		LinearRing shell = (LinearRing) fixNoding(type,p.getExteriorRing(), index, nodingResolution);
		LinearRing[] holes = new LinearRing[p.getNumInteriorRing()];
		for(int i=0; i<p.getNumInteriorRing(); i++)
			holes[i] = (LinearRing) fixNoding(type,p.getInteriorRingN(i), index, nodingResolution);
		return p.getFactory().createPolygon(shell, holes);
	}

	public static LineString fixNoding(NodingIssueType type, LineString ls, SpatialIndex index, double nodingResolution) {
		LineString out = null;
		if(type == NodingIssueType.PointPoint || type == NodingIssueType.Both)
			out = fixPPNoding(ls, index, nodingResolution);
		if(type == NodingIssueType.LinePoint || type == NodingIssueType.Both)
			out = fixLPNoding(ls, index, nodingResolution);
		return out;
	}

	//fix a noding issue by moving a coordinate (or several for closed lines) to a target position
	public static LineString fixPPNoding(LineString ls, SpatialIndex index, double nodingResolution) {
		Coordinate[] cs = ls.getCoordinates();
		Coordinate[] csOut = new Coordinate[cs.length];
		boolean found = false;
		for(int i=0; i<cs.length; i++) {
			Coordinate c_ = cs[i];
			NodingIssue ni = getPointPointNodingIssues(c, c_, nodingResolution);
			csOut[i] = ni==null? c_ : c;
			if(ni != null) found=true;
		}

		if(!found) return ls;

		if(ls.isClosed())
			return new GeometryFactory().createLinearRing(csOut);
		else
			return new GeometryFactory().createLineString(csOut);
	}

	public static LineString fixLPNoding(LineString ls, SpatialIndex index, double nodingResolution) {
		//get coordinates with noding issues and solve them

	}

	//fix a noding issue by including a coordinate located on a segment into the line geometry
	public static LineString fixLPNoding(LineString ls, Coordinate c, double nodingResolution) {
		Coordinate[] cs = ls.getCoordinates();
		Coordinate[] csOut = new Coordinate[cs.length+1];
		csOut[0] = cs[0];
		Coordinate c1 = cs[0], c2;
		boolean found = false;
		for(int i=1; i<cs.length; i++) {
			c2 = cs[i];

			if(!found && checkLPNodingIssue(c,c1,c2,nodingResolution)) {
				//insert c
				csOut[i] = c;
				found = true;
			}
			csOut[i+(found?1:0)] = cs[i];

			c1 = c2;
		}

		//LOGGER.trace(found);
		if(!found) {
			LOGGER.warn("Could not fix line-point noding issue around "+c);
			return ls;
		}

		if(ls.isClosed())
			return new GeometryFactory().createLinearRing(csOut);
		else
			return new GeometryFactory().createLineString(csOut);
	}









	public static void main(String[] args) {
		LOGGER.info("Start");

		/*
		Polygon p1 = JTSGeomUtil.createPolygon(0,0, 1,0, 0,1, 0,0);
		Polygon p2 = JTSGeomUtil.createPolygon(1,0, 0.5,0.5, 1,1, 1,0);

		System.out.println(p1);
		System.out.println(p2);
		for(NodingIssue ni : getNodingIssues(NodingIssueType.LinePoint, p1,p2, 1e-3)) System.out.println(ni);

		p1 = fixNoding(NodingIssueType.LinePoint, p1, new Coordinate(0.5, 0.5), 1e-3);
		System.out.println(p1);
		System.out.println(p2);
		for(NodingIssue ni : getNodingIssues(NodingIssueType.LinePoint, p1,p2, 1e-3)) System.out.println(ni);
		 */

		/*
		Polygon p1 = JTSGeomUtil.createPolygon(0,0.9999989, 0,0, 1.0000001,0, 0,0.9999989);
		Polygon p2 = JTSGeomUtil.createPolygon(1,0, 0,1, 1,1, 1,0);
		System.out.println(p1);
		System.out.println(p2);
		for(NodingIssue ni : getNodingIssues(NodingIssueType.PointPoint, p1,p2, 1e-3)) System.out.println(ni);

		p1 = fixNoding(NodingIssueType.PointPoint, p1, new Coordinate(1,0), 1e-3);
		p1 = fixNoding(NodingIssueType.PointPoint, p1, new Coordinate(0,1), 1e-3);
		System.out.println(p1);
		System.out.println(p2);
		for(NodingIssue ni : getNodingIssues(NodingIssueType.PointPoint, p1,p2, 1e-3)) System.out.println(ni);
		 */

		LOGGER.info("End");
	}
}
