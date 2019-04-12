/**
 * 
 */
package org.opencarto.edgematching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
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
	public final static Logger LOGGER = Logger.getLogger(NetworkEdgeMatching.class.getName());

	private ArrayList<Feature> secs;
	private HashMap<String,Double> resolutions;
	private double mult = 1.5;
	private String cntAtt = "CNTR";
	private boolean tag = false;

	//matching edges
	private ArrayList<Edge> mes;
	public ArrayList<Edge> getMatchingEdges() { return mes; }

	Graph g;

	public NetworkEdgeMatching(ArrayList<Feature> sections, HashMap<String,Double> resolutions, double mult, String cntAtt, boolean tag) {
		this.secs = sections;
		this.resolutions = resolutions;
		this.mult = mult;
		this.cntAtt = cntAtt;
		this.tag = tag;
	}


	//compute the edge matching based on matching edges
	public void makeEdgeMatching() {

		LOGGER.info("Ensure input geometries are simple");
		FeatureUtil.ensureGeometryNotAGeometryCollection(secs);

		LOGGER.info("Initialise EM tag");
		if(tag) for(Feature s : secs) s.getProperties().put("EM", "");

		LOGGER.info("Clip with buffer difference of all sections, depending on country resolution");
		makeEdgeMatchingBufferClipping();

		LOGGER.info("Build matching edges");
		buildMatchingEdges();

		LOGGER.info("Extend sections with matching edges");
		extendSectionswithMatchingEdges();
	}


	//build graph, get all nodes which are close to nodes from another cnt. Create and return new edges linking these nodes.
	private void buildMatchingEdges() {

		//create graph structure
		g = GraphBuilder.buildForNetworkFromLinearFeaturesNonPlanar(secs);

		mes = new ArrayList<>();
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
	}

	private void extendSectionswithMatchingEdges() {

		//handle special case with triangular structure with 2 matching edges, that arrive to the same node.
		for(Node n : g.getNodes()) {
			ArrayList<Edge> mes_ = getMatchingEdges(n);
			if(mes_.size() <= 1) continue;
			if(mes_.size() == 2) {
				//check if both matching edges have a section in common. If so, remove the longest matching edge.
				Iterator<Edge> it = mes_.iterator();
				Edge me1 = it.next(), me2 = it.next();
				Node n1 = me1.getN1()==n?me1.getN2():me1.getN1();
				Node n2 = me2.getN1()==n?me2.getN2():me2.getN1();
				//is there an edge between n1 and n2?
				HashSet<Edge> inter = new HashSet<Edge>();
				inter.addAll(n1.getEdges()); inter.retainAll(n2.getEdges());
				if(inter.size() == 0) continue;
				//remove longest matching edge
				Edge meToRemove = me1.getGeometry().getLength() > me2.getGeometry().getLength() ? me1 : me2;
				mes.remove(meToRemove); g.remove(meToRemove);
			}
		}

		//normal case
		for(Edge me : mes) {

			//get candidate section to extend
			Node n1 = me.getN1(), n2 = me.getN2();

			//no way to extend
			if(n1.getEdges().size()>2 && n2.getEdges().size()>2) {
				LOGGER.warn("No extension possible around "+me.getGeometry().getCentroid().getCoordinate());
				//TODO create new section from matching edge (keep attributes of one of the random sections)?
				continue;
			}

			Feature sectionToExtend = null;
			if(n2.getEdges().size()>2)
				sectionToExtend = getSectionToExtend(n1.getEdges(), me);
			else if(n1.getEdges().size()>2)
				sectionToExtend = getSectionToExtend(n2.getEdges(), me);
			else {
				//get section with worst resolution
				Feature s1 = getSectionToExtend(n1.getEdges(), me);
				Feature s2 = getSectionToExtend(n2.getEdges(), me);
				double res1 = resolutions.get(s1.get(cntAtt));
				double res2 = resolutions.get(s2.get(cntAtt));
				sectionToExtend = res1>res2? s2 : s1;
			}

			//extend section
			LineString g = null;
			try {
				g = extendLineString((LineString)sectionToExtend.getGeom(), me.getCoords());
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			if(tag) sectionToExtend.setGeom(g);
		}
	}
	private ArrayList<Edge> getMatchingEdges(Node n) {
		ArrayList<Edge> mes_ = new ArrayList<Edge>();
		for(Edge e : n.getEdges()) if(e.obj == null) mes_.add(e);
		return mes_;
	}


	private static Feature getSectionToExtend(Set<Edge> edges, Edge me) {

		//check
		if(edges.size() != 2) {
			LOGGER.error("Unexpected number of edges when getSectionToExtend around " + me.getGeometry().getCentroid().getCoordinate());
			return null;
		}

		Iterator<Edge> it = edges.iterator();
		Edge e = it.next();
		if(e == me)
			return (Feature) it.next().obj;
		return (Feature) e.obj;
	}


	//clip network section geometries with buffer of other network geometries having a better resolution
	private void makeEdgeMatchingBufferClipping() {

		//build spatial index
		Quadtree si = getSectionSI();

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
				//TODO should we really do that? Case when 2 section cross...
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
		secs = out;
	}





	//Extend line from a segment. The segment is supposed to be an extention of the line.
	private static LineString extendLineString(LineString ls, Coordinate[] segment) throws Exception {
		LineString comp = ls.getFactory().createLineString(segment);
		LineMerger lm = new LineMerger();
		lm.add(ls); lm.add(comp);
		Collection<?> lss = lm.getMergedLineStrings();
		if(lss.size() != 1) {
			LOGGER.error("Unexpected number of merged lines: "+lss.size()+" (expected value: 1).");
			for(Object l : lss) LOGGER.error(l);
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

	private Quadtree getSectionSI() {
		Quadtree si = new Quadtree();
		for(Feature c : secs) si.insert(c.getGeom().getEnvelopeInternal(), c);
		return si;
	}

}
