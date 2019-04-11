/**
 * 
 */
package org.opencarto.edgematching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.GraphBuilder;
import org.opencarto.datamodel.graph.Node;
import org.opencarto.util.FeatureUtil;

/**
 * Functions to compute edgematching of network data.
 * 
 * 
 * @author julien Gaffuri
 *
 */
public class NetworkEdgeMatching {


	//compute the edge matching based on matching edges
	//return the matching edges
	public static ArrayList<Edge> edgeMatch(ArrayList<Feature> secs, HashMap<String,Double> resolutions, double mult, String cntAtt, boolean tag) {
		System.out.println("Ensure input geometries are simple");
		FeatureUtil.ensureGeometryNotAGeometryCollection(secs);

		System.out.println("Initialise EM tag");
		if(tag) for(Feature s : secs) s.getProperties().put("EM", "");

		System.out.println("Clip with buffer difference of all sections, depending on country resolution");
		secs = clip(secs, resolutions, cntAtt, tag);

		System.out.println("Build matching edges");
		ArrayList<Edge> mes = getMatchingEdges(secs, resolutions, mult, cntAtt);

		System.out.println("Extend sections with matching edges");
		extendSectionswithMatchingEdges(mes, resolutions, cntAtt, tag);

		return mes;
	}


	//build graph, get all nodes which are close to nodes from another cnt. Create and return new edges linking these nodes.
	private static ArrayList<Edge> getMatchingEdges(ArrayList<Feature> secs, HashMap<String,Double> resolutions, double mult, String cntAtt) {
		//create graph structure
		Graph g = GraphBuilder.buildForNetworkFromLinearFeaturesNonPlanar(secs);
		ArrayList<Edge> mes = new ArrayList<>();
		for(Node n : g.getNodes()) {

			//exclude nodes that are already connecting edges from different countries
			if(connectsSeveralCountries(n, cntAtt)) continue;
			String cnt = ((Feature)n.getEdges().iterator().next().obj).get(cntAtt).toString();
			double res = mult * resolutions.get(cnt);

			//get all nodes nearby that are from another country
			for(Node n_ : g.getNodesAt(n.getGeometry().buffer(res).getEnvelopeInternal()) ) {
				if(n==n_) continue;
				if(n.getC().distance(n_.getC()) > res) continue;
				if(connectsSeveralCountries(n_, cntAtt)) continue;
				String cnt_ = ((Feature)n_.getEdges().iterator().next().obj).get(cntAtt).toString();
				if(cnt.equals(cnt_)) continue;

				//build matching edge
				Edge e = g.buildEdge(n, n_);
				mes.add(e);
			}
		}
		return mes;
	}

	private static void extendSectionswithMatchingEdges(Collection<Edge> mes, HashMap<String,Double> resolutions, String cntAtt, boolean tag) {
		for(Edge me : mes) {
			//get candidate section to extend
			Node n1 = me.getN1(), n2 = me.getN2();

			//no way to prolong
			if(n1.getEdges().size()>2 && n2.getEdges().size()>2) {
				System.out.println("No prolong possible around "+me.getGeometry().getCentroid().getCoordinate());
				continue;
			}

			Feature sectionToProlong = null;
			if(n2.getEdges().size()>2)
				sectionToProlong = getSectionToProlong(n1.getEdges(), me);
			else if(n1.getEdges().size()>2)
				sectionToProlong = getSectionToProlong(n2.getEdges(), me);
			else {
				//prolong the section with worst resolution
				Feature s1 = getSectionToProlong(n1.getEdges(), me);
				Feature s2 = getSectionToProlong(n2.getEdges(), me);
				double res1 = resolutions.get(s1.get(cntAtt));
				double res2 = resolutions.get(s2.get(cntAtt));
				sectionToProlong = res1>res2? s2 : s1;
			}

			//prolong section
			LineString g = null;
			try {
				g = prolongLineString((LineString)sectionToProlong.getGeom(), me.getCoords());
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			if(tag) sectionToProlong.setGeom(g);
		}
	}

	private static Feature getSectionToProlong(Set<Edge> edges, Edge me) {
		//check
		if(edges.size() != 2) {
			System.err.println("Unexpected number of edges when getSectionToProlong around " + me.getGeometry().getCentroid().getCoordinate());
			return null;
		}
		Iterator<Edge> it = edges.iterator();
		Edge e = it.next();
		if(e == me)
			return (Feature) it.next().obj;
		return (Feature) e.obj;
	}


	//clip network section geometries with buffer of other network geometries having a better resolution
	private static ArrayList<Feature> clip(ArrayList<Feature> secs, HashMap<String,Double> resolutions, String cntAtt, boolean tag) {

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
				if(tag) s_.set("EM", s_.get("EM")+"i");
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
				if(tag) s.set("EM", s.get("EM")+"c");
				out.add(s);
			} else {
				//TODO should we really do that?
				MultiLineString mls = (MultiLineString)g;
				for(int i=0; i<mls.getNumGeometries(); i++) {
					Feature f = new Feature();
					f.setGeom((LineString) mls.getGeometryN(i));
					f.getProperties().putAll(s.getProperties());
					if(tag) f.set("EM", f.get("EM")+"c");
					out.add(f);
					si.insert(f.getGeom().getEnvelopeInternal(), f);
				}
			}

		}
		return out;
	}





	/*/connect ls1 to nearest point of ls2. Return the prolongates line of ls1 to nearest point of ls2.
	private static LineString connectLineStringsTip(LineString ls1, LineString ls2, double threshold) throws Exception {

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
	}*/



	//Prolonge line from a segment. The segment is supposed to be a prolongation of the line.
	private static LineString prolongLineString(LineString ls, Coordinate[] segment) throws Exception {
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
	private static boolean areConnected(LineString ls1, LineString ls2) {
		Coordinate[] cs1 = ls1.getCoordinates(), cs2 = ls2.getCoordinates();
		if(cs1[0].distance(cs2[0]) == 0) return true;
		if(cs1[0].distance(cs2[cs2.length-1]) == 0) return true;
		if(cs1[cs1.length-1].distance(cs2[0]) == 0) return true;
		if(cs1[cs1.length-1].distance(cs2[cs2.length-1]) == 0) return true;
		return false;
	}




	//check if a node has edges from different countries
	private static boolean connectsSeveralCountries(Node n, String cntAtt) {
		String cnt = null;
		for(Edge e : n.getEdges()) {
			if(e.obj == null) return true;
			if(cnt == null) {
				cnt = ((Feature)e.obj).get(cntAtt).toString();
				continue;
			}
			if( !((Feature)e.obj).get(cntAtt).toString().equals(cnt) ) return true;
		}
		return false;
	}

}
