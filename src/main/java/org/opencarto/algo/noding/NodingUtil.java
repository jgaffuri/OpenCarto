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
	private final static Logger LOGGER = Logger.getLogger(NodingUtil.class.getName());


	public enum NodingIssueType { PointPoint, LinePoint }

	public static class NodingIssue{
		public NodingIssueType type;
		public Coordinate c;
		public double distance;
		public NodingIssue(NodingIssueType type, Coordinate c, double distance) { this.type=type; this.c=c; this.distance=distance; }
		public String toString() { return type+" c="+c+" d="+distance; }
	}



	//get noding issues for polygonal features
	public static Collection<NodingIssue> getNodingIssues(Collection<Feature> mpfs, double nodingResolution) {
		Collection<NodingIssue> nis = new HashSet<NodingIssue>();
		STRtree index = Feature.getSTRtree(mpfs);
		for(Feature mpf : mpfs) {
			LOGGER.trace(mpf.id);
			nis.addAll(getNodingIssues(NodingIssueType.PointPoint, mpf, index, nodingResolution));
			nis.addAll(getNodingIssues(NodingIssueType.LinePoint, mpf, index, nodingResolution));
		}
		return nis;
	}

	public static Collection<NodingIssue> getNodingIssues(NodingIssueType type, Collection<Feature> mpfs, double nodingResolution) {
		Collection<NodingIssue> nis = new HashSet<NodingIssue>();
		STRtree index = Feature.getSTRtree(mpfs);
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


	//check if segments of 1 are fragmented enough to snap to points of 2
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

	//check if segments of 1 are fragmented enough to snap to points of 2
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
		//noded case ok
		if( c.distance(c1) <= nodingResolution ) return null;
		if( c.distance(c2) <= nodingResolution ) return null;
		//not noded case ok
		double d = new LineSegment(c1,c2).distance(c);
		if( d > nodingResolution ) return null;
		return new NodingIssue(NodingIssueType.LinePoint,c,d);
	}

	public static NodingIssue getPointPointNodingIssues(Coordinate c, Coordinate c_, double nodingResolution) {
		double d = c.distance(c_);
		if( d==0 || d > nodingResolution ) return null;
		return new NodingIssue(NodingIssueType.PointPoint,c,d);
	}







	/*public static Collection<NodingIssue> getNodingIssuesBoth(Geometry g1, Geometry g2) {
		Collection<NodingIssue> out = new HashSet<NodingIssue>();
		out.addAll(getNodingIssues(g1,g2)); out.addAll(getNodingIssues(g2,g1));
		return out;
	}*/

	//generic but highly inefficient method
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
	}





	//TODO make fixing more efficient with spatial indexing

	public static MultiPolygon fixNoding(NodingIssueType type, MultiPolygon mp, Coordinate c, double nodingResolution) {
		Polygon[] ps = new Polygon[mp.getNumGeometries()];
		for(int i=0; i<mp.getNumGeometries(); i++)
			ps[i] = fixNoding(type, (Polygon) mp.getGeometryN(i), c, nodingResolution);
		return new GeometryFactory().createMultiPolygon(ps);
	}


	public static Polygon fixNoding(NodingIssueType type, Polygon p, Coordinate c, double nodingResolution) {
		LinearRing shell = (LinearRing) fixNoding(type,p.getExteriorRing(), c, nodingResolution);
		LinearRing[] holes = new LinearRing[p.getNumInteriorRing()];
		for(int i=0; i<p.getNumInteriorRing(); i++)
			holes[i] = (LinearRing) fixNoding(type,p.getInteriorRingN(i), c, nodingResolution);
		return new GeometryFactory().createPolygon(shell, holes);
	}

	public static LineString fixNoding(NodingIssueType type, LineString ls, Coordinate c, double nodingResolution) {
		if(type == NodingIssueType.PointPoint)
			return fixPPNoding(ls, c, nodingResolution);
		else if(type == NodingIssueType.LinePoint)
			return fixLPNoding(ls, c, nodingResolution);
		return null;
	}

	public static LineString fixPPNoding(LineString ls, Coordinate c, double nodingResolution) {
		Coordinate[] cs = ls.getCoordinates();
		Coordinate[] csOut = new Coordinate[cs.length];
		boolean found = false;
		for(int i=0; i<cs.length; i++) {
			Coordinate c_ = cs[i];
			NodingIssue ni = getPointPointNodingIssues(c, c_, nodingResolution);
			csOut[i] = ni == null? c_ : c;
			if(ni != null) { found=true; }
		}

		if(!found) return ls;

		if(ls.isClosed())
			return new GeometryFactory().createLinearRing(csOut);
		else
			return new GeometryFactory().createLineString(csOut);
	}

	//fix a noding issue by including a coordinate (which is supposed to be located on a segment) into the geometry representation
	public static LineString fixLPNoding(LineString ls, Coordinate c, double nodingResolution) {
		Coordinate[] cs = ls.getCoordinates();
		Coordinate[] csOut = new Coordinate[cs.length+1];
		csOut[0] = cs[0];
		Coordinate c1 = cs[0], c2;
		boolean found = false;
		for(int i=1; i<cs.length; i++) {
			c2 = cs[i];

			//analyse segment [c1,c2]
			if(!found && new LineSegment(c1, c2).distance(c) <= nodingResolution) {
				//insert c
				csOut[i] = c;
				found = true;
			}
			csOut[i+(found?1:0)] = cs[i];

			c1 = c2;
		}

		if(!found) return ls;

		if(ls.isClosed())
			return new GeometryFactory().createLinearRing(csOut);
		else
			return new GeometryFactory().createLineString(csOut);
	}



	public static void fixNoding(Collection<Feature> mpfs, double nodingResolution) {
		//fixNoding(NodingIssueType.PointPoint, mpfs, nodingResolution); //TODO check that
		fixNoding(NodingIssueType.LinePoint, mpfs, nodingResolution);
	}

	public static void fixNoding(NodingIssueType type, Collection<Feature> mpfs, double nodingResolution) {
		STRtree index = Feature.getSTRtree(mpfs);
		for(Feature mpf : mpfs)
			fixNoding(type, mpf, index, nodingResolution);
	}


	public static void fixNoding(NodingIssueType type, Feature mpf, SpatialIndex index, double nodingResolution) {
		Collection<NodingIssue> nis = NodingUtil.getNodingIssues(type, mpf, index, nodingResolution);
		while(nis.size() > 0) {
			//System.out.println(mpf.id+" - "+nis.size());
			Coordinate c = nis.iterator().next().c;
			MultiPolygon mp = fixNoding(type, (MultiPolygon) mpf.getGeom(), c, nodingResolution);
			mpf.setGeom(mp);
			nis = NodingUtil.getNodingIssues(type, mpf, index, nodingResolution);
		}
	}












	public static void main(String[] args) {
		LOGGER.info("Start");

		/*LOGGER.setLevel(Level.ALL);
		Collection<Feature> mpfs = SHPUtil.loadSHP("/home/juju/Bureau/nuts_gene_data/commplus/COMM_PLUS_FINAL_WM_aaa.shp").fs;
		Collection<NodingIssue> nis = getNodingIssues(mpfs, 0.1);
		for(NodingIssue ni : nis)
			System.out.println(ni.toString());*/



		//LineString ls1 = JTSGeomUtil.createLineString(0,0, 1,1);
		//LineString ls2 = JTSGeomUtil.createLineString(0.5,0.5 ,1,0);
		//Collection<NodingIssue> out = getNodingIssues(ls2,ls1);

		Polygon p1 = JTSGeomUtil.createPolygon(0,0, 0,1, 1,1, 0,0);
		Polygon p2 = JTSGeomUtil.createPolygon(1e-5,0, 0.5,0.5, 1,0, 1e-5,0);

		System.out.println(p1);
		System.out.println(p2);
		for(NodingIssue ni : getNodingIssues(NodingIssueType.LinePoint, p1,p2, 1e-3)) System.out.println(ni);
		for(NodingIssue ni : getNodingIssues(NodingIssueType.PointPoint, p1,p2, 1e-3)) System.out.println(ni);

		p1 = fixNoding(NodingIssueType.LinePoint, p1, new Coordinate(0.5, 0.5), 1e-3);
		p1 = fixNoding(NodingIssueType.PointPoint, p2, new Coordinate(1.0E-5, 0.0), 1e-3);
		System.out.println(p1);
		System.out.println(p2);
		for(NodingIssue ni : getNodingIssues(NodingIssueType.LinePoint, p1,p2, 1e-3)) System.out.println(ni);
		for(NodingIssue ni : getNodingIssues(NodingIssueType.PointPoint, p1,p2, 1e-3)) System.out.println(ni);
		LOGGER.info("End");
	}
}
