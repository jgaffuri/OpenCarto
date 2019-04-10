/**
 * 
 */
package org.opencarto.edgematching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.locationtech.jts.operation.distance.DistanceOp;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.opencarto.datamodel.Feature;

/**
 * Functions to compute edgematching of network data.
 * 
 * 
 * @author julien Gaffuri
 *
 */
public class NetworkEdgeMatching {



	//clip network section geometries with buffer of other network geometries having a better resolution
	public static ArrayList<Feature> clip(ArrayList<Feature> secs, HashMap<String,Double> resolutions, String cntAtt) {

		//build spatial index
		Quadtree si = new Quadtree();
		for(Feature c : secs) si.insert(c.getGeom().getEnvelopeInternal(), c);

		//get maximum resolution
		double resMax = Collections.max(resolutions.values());

		ArrayList<Feature> out = new ArrayList<Feature>();
		for(Feature s : secs) {
			if(s.getGeom().isEmpty()) continue;

			String cnt = s.get(cntAtt).toString();
			double res = resolutions.get(cnt);
			Geometry g = s.getGeom();
			Envelope env = g.getEnvelopeInternal(); env.expandBy(resMax*1.01);

			//s to be 'cut' by sections from other countries with better resolution
			boolean changed = false;
			for(Object s2 : si.query(env)) {
				Feature s_ = (Feature) s2;

				//filter
				if(s == s_) continue;
				if(! s_.getGeom().getEnvelopeInternal().intersects(env)) continue;
				if(s_.getGeom().isEmpty()) continue;
				String cnt_ = s_.get(cntAtt).toString();
				if(cnt_.equals(cnt)) continue;
				double res_ = resolutions.get(cnt_);
				//s to be cut by those with better resolution
				if(res_ > res) continue;
				//do not cut already existing connections
				if(areConnected( (LineString)s.getGeom(), (LineString)s_.getGeom())) continue;

				Geometry buff = s_.getGeom().buffer(res);
				if(! g.intersects(buff)) continue;

				g = g.difference(buff);
				changed=true;
				s_.set("EM", s_.get("EM")+"i");
				if(g.isEmpty()) break;
			}

			if(!changed) {
				out.add(s);
				continue;
			}

			si.remove(s.getGeom().getEnvelopeInternal(), s);
			s.setGeom(g);

			if(g.isEmpty()) continue;

			if(g instanceof LineString) {
				si.insert(s.getGeom().getEnvelopeInternal(), s);
				s.set("EM", s.get("EM")+"c");
				out.add(s);
			} else {
				//TODO should we really do that?
				MultiLineString mls = (MultiLineString)g;
				for(int i=0; i<mls.getNumGeometries(); i++) {
					Feature f = new Feature();
					f.setGeom((LineString) mls.getGeometryN(i));
					f.getProperties().putAll(s.getProperties());
					f.set("EM", f.get("EM")+"c");
					out.add(f);
					si.insert(f.getGeom().getEnvelopeInternal(), f);
				}
			}

		}
		return out;
	}





	//connect ls1 to nearest point of ls2. Return the prolongates line of ls1 to nearest point of ls2.
	public static LineString connectLineStringsTip(LineString ls1, LineString ls2, double threshold) throws Exception {

		//find points extrema and connect them from t_
		DistanceOp dop = new DistanceOp(ls1, ls2);

		if(dop.distance() > threshold)
			return null;

		Coordinate[] pts = dop.nearestPoints();
		if(pts.length != 2)
			throw new Exception("Unexpected number of points encountered (2 expected) in DistanceOp ("+pts.length+") around "+pts[0]);

		//connect only from tip: check both coordinates are from extreme points
		if( pts[0].distance(ls1.getCoordinateN(0)) != 0 && pts[0].distance(ls1.getCoordinateN(ls1.getCoordinates().length-1)) != 0 ) return null;
		if( pts[1].distance(ls2.getCoordinateN(0)) != 0 && pts[1].distance(ls2.getCoordinateN(ls2.getCoordinates().length-1)) != 0 ) return null;

		return prolongLineString(ls1, pts);
	}



	//Prolonge line from a segment. The segment is supposed to be a prolongation of the line.
	public static LineString prolongLineString(LineString ls, Coordinate[] segment) throws Exception {
		LineString comp = ls.getFactory().createLineString(segment);
		LineMerger lm = new LineMerger();
		lm.add(ls); lm.add(comp);
		Collection<?> lss = lm.getMergedLineStrings();
		if(lss.size() != 1) {
			System.err.println("Unexpected number of merged lines: "+lss.size()+" (expected value: 1).");
			for(Object l : lss) System.err.println(l);
			return null;
		}
		Object out = lss.iterator().next();

		if(out instanceof LineString) return (LineString) out;
		if(out instanceof MultiLineString) {
			MultiLineString out_ = (MultiLineString) out;
			if(out_.getNumGeometries() != 1)
				throw new Exception("Unexpected number of geometries ("+out_.getNumGeometries()+" (expected value: 1).");
			return (LineString) out_.getGeometryN(0);
		}
		throw new Exception("Unexpected geometry type ("+out.getClass().getSimpleName()+". Linear geometry expected.");
	}




	//check if two linestrings are connected from at least 2 of their tips
	public static boolean areConnected(LineString ls1, LineString ls2) {
		Coordinate[] cs1 = ls1.getCoordinates(), cs2 = ls2.getCoordinates();
		if(cs1[0].distance(cs2[0]) == 0) return true;
		if(cs1[0].distance(cs2[cs2.length-1]) == 0) return true;
		if(cs1[cs1.length-1].distance(cs2[0]) == 0) return true;
		if(cs1[cs1.length-1].distance(cs2[cs2.length-1]) == 0) return true;
		return false;
	}

}
