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
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * @author julien Gaffuri
 *
 */
public class NodingUtil {
	public final static Logger LOGGER = Logger.getLogger(NodingUtil.class.getName());


	public enum NodingIssueType { PointPoint, LinePoint }

	public static class NodingIssue{
		public NodingIssueType type;
		public Coordinate c;
		public double distance;
		public NodingIssue(NodingIssueType type, Coordinate c, double distance) { this.type=type; this.c=c; this.distance=distance; }
		public String toString() { return "NodingIssue "+type+" c="+c+" d="+distance; }
	}




	private static boolean checkLPNodingIssue(Coordinate c, Coordinate c1, Coordinate c2, double nodingResolution) {
		return
				c.distance(c1) > nodingResolution
				&& c.distance(c2) > nodingResolution
				&& new LineSegment(c1,c2).distance(c) <= nodingResolution
				;
	}

	private static boolean checkPPNodingIssue(Coordinate c, Coordinate c_, double nodingResolution) {
		double d = c.distance(c_);
		return(d!=0 && d <= nodingResolution);
	}






	//get noding issues for multi-polygonal features
	public static Collection<NodingIssue> getNodingIssues(Collection<Feature> mpfs, double nodingResolution) {
		Collection<NodingIssue> nis = new HashSet<NodingIssue>();
		STRtree index = Feature.getSTRtreeCoordinates(mpfs);
		for(Feature mpf : mpfs) {
			LOGGER.trace(mpf.id);
			nis.addAll(getNodingIssues(NodingIssueType.PointPoint, mpf, index, nodingResolution));
			nis.addAll(getNodingIssues(NodingIssueType.LinePoint, mpf, index, nodingResolution));
		}
		return nis;
	}

	public static Collection<NodingIssue> getNodingIssues(NodingIssueType type, Collection<Feature> mpfs, double nodingResolution) {
		STRtree index = Feature.getSTRtreeCoordinates(mpfs);
		Collection<NodingIssue> nis = new HashSet<NodingIssue>();
		for(Feature mpf : mpfs)
			nis.addAll(getNodingIssues(type, mpf, index, nodingResolution));
		return nis;
	}

	public static Collection<NodingIssue> getNodingIssues(Feature mpf, SpatialIndex index, double nodingResolution) {
		Collection<NodingIssue> nis = new HashSet<NodingIssue>();
		nis.addAll(getNodingIssues(NodingIssueType.PointPoint, mpf, index, nodingResolution));
		nis.addAll(getNodingIssues(NodingIssueType.LinePoint, mpf, index, nodingResolution));
		return nis;
	}

	public static Collection<NodingIssue> getNodingIssues(NodingIssueType type, Feature mpf, SpatialIndex index, double nodingResolution) {
		return getNodingIssues(type, (MultiPolygon)mpf.getGeom(), index, nodingResolution);
	}

	public static Collection<NodingIssue> getNodingIssues(NodingIssueType type, MultiPolygon mp, SpatialIndex index, double nodingResolution) {
		Collection<NodingIssue> out = new HashSet<NodingIssue>();
		for(int i=0; i<mp.getNumGeometries(); i++)
			out.addAll( getNodingIssues(type,(Polygon) mp.getGeometryN(i),index,nodingResolution) );
		return out;
	}

	public static Collection<NodingIssue> getNodingIssues(NodingIssueType type, Polygon p, SpatialIndex index, double nodingResolution) {
		Collection<NodingIssue> out = new HashSet<NodingIssue>();
		for(LineString lr : JTSGeomUtil.getRings(p))
			out.addAll( getNodingIssues(type,lr,index,nodingResolution) );
		return out;
	}



	private static Collection<NodingIssue> getNodingIssues(NodingIssueType type, LineString ls, SpatialIndex index, double nodingResolution) {

		Collection<NodingIssue> out = new HashSet<NodingIssue>();

		if(type == NodingIssueType.LinePoint ) {
			//go through segments of l1
			Coordinate[] c1s = ls.getCoordinates();
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
		} else if(type == NodingIssueType.PointPoint ) {
			//go through coordinates of l1
			Coordinate[] c1s = ls.getCoordinates();
			for(int i=0; i<c1s.length+(ls.isClosed()?-1:0); i++) {
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

	public static NodingIssue getPointPointNodingIssues(Coordinate c, Coordinate c_, double nodingResolution) {
		if( checkPPNodingIssue(c,c_,nodingResolution) )
			return new NodingIssue(NodingIssueType.PointPoint,c,c.distance(c_));
		else
			return null;
	}








	public static void fixNoding(NodingIssueType type, Collection<Feature> mpfs, double nodingResolution) {
		STRtree index = type==NodingIssueType.LinePoint? Feature.getSTRtreeCoordinates(mpfs) : getSTRtreeCoordinatesForPP(mpfs, nodingResolution);
		for(Feature mpf : mpfs)
			fixNoding(type, mpf, index, nodingResolution);
	}



	private static void fixNoding(NodingIssueType type, Feature mpf, SpatialIndex index, double nodingResolution) {
		MultiPolygon mp = fixNoding(type, (MultiPolygon) mpf.getGeom(), index, nodingResolution);
		mpf.setGeom(mp);
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
		LineString out = ls;
		//for(NodingIssue ni : nis) out = fixNoding(ni.type, out, ni.c, nodingResolution);
		//fix the noding issues until it is all solved
		Collection<NodingIssue> nis = getNodingIssues(type, ls, index, nodingResolution);
		while(nis.size() != 0) {
			NodingIssue ni = nis.iterator().next();
			out = fixNoding(ni.type, out, ni.c, nodingResolution);
			nis = getNodingIssues(type, out, index, nodingResolution);
		}
		return out;
	}


	public static LineString fixNoding(NodingIssueType type, LineString ls, Coordinate c, double nodingResolution) {
		LineString out = null;
		if(type == NodingIssueType.PointPoint)
			out = fixPPNoding(ls, c, nodingResolution);
		if(type == NodingIssueType.LinePoint)
			out = fixLPNoding(ls, c, nodingResolution);
		return out;
	}

	//fix a noding issue by moving a coordinate (or several for closed lines) to a target position
	public static LineString fixPPNoding(LineString ls, Coordinate c, double nodingResolution) {

		System.out.println("------");
		System.out.println(ls);
		System.out.println(c);

		Coordinate[] cs = ls.getCoordinates();
		Coordinate[] csOut = new Coordinate[cs.length];
		boolean found = false;
		for(int i=0; i<cs.length; i++) {
			Coordinate c_ = cs[i];

			System.out.println(c_);

			boolean issue = checkPPNodingIssue(c, c_, nodingResolution);

			System.out.println(issue);

			csOut[i] = issue? c : c_;
			if(issue) found = true;
		}

		if(!found) {
			LOGGER.warn("Could not fix line-point noding issue around "+c);
			return ls;
		}

		System.out.println(ls.getFactory().createLinearRing(csOut));
		System.out.println("------");

		if(ls.isClosed())
			return ls.getFactory().createLinearRing(csOut);
		else
			return ls.getFactory().createLineString(csOut);
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

		if(!found) {
			LOGGER.warn("Could not fix line-point noding issue around "+c);
			return ls;
		}

		if(ls.isClosed())
			return ls.getFactory().createLinearRing(csOut);
		else
			return ls.getFactory().createLineString(csOut);
	}









	private static STRtree getSTRtreeCoordinatesForPP(Collection<Feature> fs, double nodingResolution) {
		Collection<Geometry> geoms = new HashSet<Geometry>();
		for(Feature f : fs) geoms.add(f.getGeom());
		return getSTRtreeCoordinatesForPPG(geoms, nodingResolution);
	}
	private static SpatialIndex getSTRtreeCoordinatesForPP(double nodingResolution, Geometry... geoms) {
		Collection<Geometry> gs = new HashSet<Geometry>();
		for(Geometry g : geoms) gs.add(g);
		return getSTRtreeCoordinatesForPPG(gs, nodingResolution);
	}

	private static STRtree getSTRtreeCoordinatesForPPG(Collection<Geometry> gs, double nodingResolution) {
		//build index of all coordinates
		Quadtree index = new Quadtree();
		for(Geometry g : gs) {
			Coordinate[] cs_ = g.getCoordinates();
			for(Coordinate c : cs_)
				index.insert(new Envelope(c), c);
		}

		//find couple of coordinates to merge
		Coordinate[] sn = findCoupleToSnap(index, nodingResolution);
		while(sn != null) {
			//merge coordinates
			Coordinate c1 = sn[0], c2 = sn[1];
			Coordinate c = new Coordinate((c1.x+c2.x)*0.5, (c1.y+c2.y)*0.5);
			boolean b;
			b = index.remove(new Envelope(c1), c1); if(!b) LOGGER.warn("Pb when merging points (index) around "+c1);
			b = index.remove(new Envelope(c2), c2); if(!b) LOGGER.warn("Pb when merging points (index) around "+c2);
			index.insert(new Envelope(c), c);

			//find new couple of coordinates to merge
			sn = findCoupleToSnap(index, nodingResolution);
		}

		STRtree index_ = new STRtree();
		for(Coordinate c : (List<Coordinate>)index.queryAll()) index_.insert(new Envelope(c), c);
		return index_;
	}
	private static Coordinate[] findCoupleToSnap(Quadtree index, double nodingResolution) {
		for(Coordinate c1 : (List<Coordinate>)index.queryAll()) {
			Envelope env = new Envelope(c1); env.expandBy(nodingResolution*1.01);
			for(Coordinate c2 : (List<Coordinate>)index.query(env ))
				if(c1.distance(c2) <= nodingResolution) return new Coordinate[]{c1,c2};
		}
		return null;
	}





	/*private static SpatialIndex getCoordinatesSpatialIndex(Geometry... geoms) {
		STRtree index = new STRtree();
		for(Geometry g : geoms) {
			for(Coordinate c : g.getCoordinates())
				index.insert(new Envelope(c), c);
		}
		return index;
	}*/

	public static void main(String[] args) {
		LOGGER.info("Start");

		double nodingResolution = 1e-3;


		Polygon p1 = JTSGeomUtil.createPolygon(0,0, 1,0, 0,1, 0,0);
		Polygon p2 = JTSGeomUtil.createPolygon(1,0, 0.5,0.5, 1,1, 1,0);
		//SpatialIndex index = getCoordinatesSpatialIndex(p1, p2);
		SpatialIndex index = getSTRtreeCoordinatesForPP(nodingResolution, p1, p2);

		System.out.println(p1);
		System.out.println(p2);
		for(NodingIssue ni : getNodingIssues(NodingIssueType.LinePoint, p1, index, nodingResolution)) System.out.println(ni);

		p1 = fixNoding(NodingIssueType.LinePoint, p1, index, nodingResolution);
		System.out.println(p1);
		System.out.println(p2);
		for(NodingIssue ni : getNodingIssues(NodingIssueType.LinePoint, p1, index, nodingResolution)) System.out.println(ni);


		/*
		Polygon p1 = JTSGeomUtil.createPolygon(0,1, 0,0, 1.00001,0, 0,1);
		Polygon p2 = JTSGeomUtil.createPolygon(1,0, 0,1, 1,1, 1,0);
		SpatialIndex index = getSTRtreeCoordinatesForPP(nodingResolution, p1, p2);

		System.out.println(p1);
		System.out.println(p2);
		for(NodingIssue ni : getNodingIssues(NodingIssueType.PointPoint, p1, index, nodingResolution)) System.out.println(ni);

		p1 = fixNoding(NodingIssueType.PointPoint, p1, index, nodingResolution);
		System.out.println(p1);
		System.out.println(p2);
		for(NodingIssue ni : getNodingIssues(NodingIssueType.PointPoint, p1, index, nodingResolution)) System.out.println(ni);
		 */
		LOGGER.info("End");
	}

}
