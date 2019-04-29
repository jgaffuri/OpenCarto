/**
 * 
 */
package org.opencarto.algo.edgematching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.opencarto.algo.graph.GraphConnexComponents;
import org.opencarto.algo.graph.GraphConnexComponents.EdgeFilter;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.GraphBuilder;
import org.opencarto.datamodel.graph.Node;
import org.opencarto.util.FeatureUtil;

/**
 * Functions to compute edgematching of network data.
 * 
 * @author julien Gaffuri
 *
 */
public class NetworkEdgeMatching {
	public final static Logger LOGGER = Logger.getLogger(NetworkEdgeMatching.class.getName());

	//the sections
	private ArrayList<Feature> secs;
	public Collection<? extends Feature> getSections() { return secs; }
	//the attribute tag for the different regions
	private String cntAtt = "CNTR";
	//the resolution of the different regions, in meter. If no resolution is specified, the default value is set to 1.0m.
	private HashMap<String,Double> resolutions = null;
	//a multiplication parameter to increase the snapping distance. Set to 1.
	private double mult = 1.5;
	//set to true if the output sections should be tagged depending on the way they are handled in the edge matching procedure
	private boolean tagOutput = false;

	//the matching edges: edges created during the process, which link two tips of sections that do not belong to the same country, close to each other, and not already connected
	private ArrayList<Edge> mes;
	public ArrayList<Edge> getMatchingEdges() { return mes; }

	//the graph structure used during the process
	private Graph g;


	public NetworkEdgeMatching(ArrayList<Feature> sections) { this(sections, null, 1.5, "CNTR", false); }
	public NetworkEdgeMatching(ArrayList<Feature> sections, HashMap<String,Double> resolutions, double mult, String cntAtt, boolean tag) {
		this.secs = sections;
		this.resolutions = resolutions;
		this.mult = mult;
		this.cntAtt = cntAtt;
		this.tagOutput = tag;
	}


	//compute the edge matching based on matching edges
	public void makeEdgeMatching() {

		LOGGER.info("Ensure input geometries are simple");
		FeatureUtil.ensureGeometryNotAGeometryCollection(secs);

		if(tagOutput) {
			LOGGER.info("Initialise EM tag");
			for(Feature s : secs) s.set("EM", "");
		}

		LOGGER.info("Clip with buffer difference of all sections, depending on country resolution");
		makeEdgeMatchingBufferClipping();

		LOGGER.info("Create graph structure");
		g = GraphBuilder.buildForNetworkFromLinearFeaturesNonPlanar(secs);

		LOGGER.info("Build matching edges");
		buildMatchingEdges();

		LOGGER.info("Filter matching edges");
		filterMatchingEdges();

		LOGGER.info("Extend sections with matching edges");
		extendSectionswithMatchingEdges();
	}


	private double getResolution(String cnt) {
		if(resolutions == null) return 1.0;
		Double res = resolutions.get(cnt);
		if(res == null) {
			LOGGER.warn("Could not find resolution value for " + cnt);
			return 1.0;
		}
		return res.doubleValue();
	}



	//clip network section geometries with buffer of other network geometries having a better resolution
	private void makeEdgeMatchingBufferClipping() {

		//build spatial index
		Quadtree si = FeatureUtil.getQuadtree(secs);

		//get maximum resolution
		double resMax = resolutions==null? 1.0 : Collections.max(resolutions.values());

		ArrayList<Feature> secsToCheck = new ArrayList<Feature>();
		secsToCheck.addAll(secs);
		while(secsToCheck.size() > 0) {
			Feature s = secsToCheck.get(0);
			secsToCheck.remove(s);

			if(s.getGeom().isEmpty()) {
				secs.remove(s);
				continue;
			}

			String cnt = s.get(cntAtt).toString();
			double res = getResolution(cnt);
			Geometry g = (LineString) s.getGeom();

			//s to be 'cut' by nearby sections from other countries with better resolution
			boolean changed = false;
			Envelope env = g.getEnvelopeInternal(); env.expandBy(resMax*1.01);
			for(Object s2 : si.query(env)) {
				Feature s_ = (Feature) s2;
				LineString ls_ = (LineString) s_.getGeom();

				//filter
				if(s == s_) continue;
				if(ls_.isEmpty()) continue;
				if(! ls_.getEnvelopeInternal().intersects(env)) continue;
				String cnt_ = s_.get(cntAtt).toString();
				if(cnt_.equals(cnt)) continue;
				if(getResolution(cnt_) > res) continue; //s to be cut by those with better resolution only

				//compute buffer
				Geometry buff = ls_.buffer(res);
				if(! g.intersects(buff)) continue;


				g = g.difference(buff);
				changed = true;
				if(tagOutput) s_.set("EM", "bufferInvolved");
				if(g.isEmpty()) break;
			}

			if(!changed) continue;

			boolean b = si.remove(s.getGeom().getEnvelopeInternal(), s);
			if(!b) LOGGER.warn("Failed removing object from spatial index");

			if(g.isEmpty()) {
				secs.remove(s);
				continue;
			}

			if(g instanceof LineString) {
				s.setGeom(g);
				si.insert(s.getGeom().getEnvelopeInternal(), s);
				if(tagOutput) s.set("EM", "bufferClipped");
			} else {
				//TODO should we really do that? Consider case when 2 sections of different countries cross...
				//TODO issue in PT ...
				MultiLineString mls = (MultiLineString)g;
				for(int i=0; i<mls.getNumGeometries(); i++) {
					Feature f = new Feature();
					f.setGeom( (LineString)mls.getGeometryN(i) );
					f.getProperties().putAll(s.getProperties());
					if(tagOutput) f.set("EM", "bufferClipped");
					secs.add(f);
					secsToCheck.add(f); //TODO ?
					si.insert(f.getGeom().getEnvelopeInternal(), f);
				}
			}
		}
	}




	//build matching edges
	private void buildMatchingEdges() {

		//label nodes with countries
		for(Node n : g.getNodes()) {
			String cnt = getEdgesCountry(n);
			if(cnt==null) LOGGER.warn("Could not determine country for node around " + n.getC());
			n.obj = cnt;
		}

		//initialise collection of matching edges
		if(mes == null) mes = new ArrayList<>(); else mes.clear();

		//connect nodes from different countries that are nearby
		for(Node n : g.getNodes()) {
			String cnt = n.obj.toString();
			double res = mult * getResolution(cnt);

			//get other nodes nearby that are from another country
			for(Node n_ : g.getNodesAt(n.getGeometry().buffer(res*1.01).getEnvelopeInternal()) ) {
				if(n==n_) continue;
				if(n.getC().distance(n_.getC()) > res) continue;
				if(cnt.equals(n_.obj.toString())) continue;

				//exclude already connected nodes
				if( g.getEdge(n, n_) != null || g.getEdge(n_, n) != null ) continue;

				//build matching edge
				Edge e = g.buildEdge(n, n_);
				mes.add(e);
			}
		}
	}

	//check that all edges of a node have the same country and return it.
	//if there is no edges or some edges have different countries, return null.
	private String getEdgesCountry(Node n) {
		String cnt = null;
		for(Edge e : n.getEdges()) {
			if(e.obj == null) continue;
			if(cnt == null) {
				cnt = ((Feature)e.obj).get(cntAtt).toString();
				continue;
			}
			if( !((Feature)e.obj).get(cntAtt).toString().equals(cnt))
				return null;
		}
		return cnt;
	}




	//filter matching edges based on several criteria.
	private void filterMatchingEdges() {

		//get connex components of matching edges
		Collection<Graph> gcc = GraphConnexComponents.get(g, new EdgeFilter() {
			@Override
			public boolean keep(Edge e) { return e.obj == null; } //keep only the matching edges
		}, true);

		//go through the connex components
		for(Graph cc : gcc) {
			if(cc.getEdges().size() == 1) continue;

			//TODO do something for cases with more than 3 edges? remove the longest(s)? check intersections?
			//TODO break connex components? (by detecting isthmus?) maybe it is general to size=3 also

			if(cc.getEdges().size() == 3) {
				Iterator<Edge> it = cc.getEdges().iterator();
				Edge me1=it.next(), me2=it.next(), me3=it.next();
				double d1=me1.getGeometry().getLength(), d2=me2.getGeometry().getLength(), d3=me3.getGeometry().getLength();
				if(cc.getNodes().size() == 3) {
					//triangle case: remove longest edge
					Edge meToRemove = (d1>d2&&d1>d3)? me1 : (d2>d1&&d2>d3) ? me2 : me3;
					mes.remove(meToRemove); g.remove(meToRemove);
				} else if(cc.getNodes().size() == 4) {
					Node n1 = cc.areConnected(me2, me3), n2 = cc.areConnected(me3, me1), n3 = cc.areConnected(me1, me2);
					if( n1==null || n2==null || n3==null ) {
						//line structure: remove the one in the middle
						Edge meToRemove = n1==null? me1 : n2==null? me2 : me3;
						mes.remove(meToRemove); g.remove(meToRemove);
					} else if(n1==n2 && n2==n3) {
						//star structure:do nothing
					}
				}
			}

			if(cc.getEdges().size() == 2) {
				//handle special case with triangular structure with 2 matching edges, that arrive to the same node.
				//in such case, the longest matching edge is removed
				Iterator<Edge> it = cc.getEdges().iterator();
				Edge me1 = it.next(), me2 = it.next();
				Node n = me1.getN1()==me2.getN1()||me1.getN1()==me2.getN2()?me1.getN1() : me1.getN2()==me2.getN1()||me1.getN2()==me2.getN2()?me1.getN2() : null;
				Node n1 = me1.getN1()==n?me1.getN2():me1.getN1();
				Node n2 = me2.getN1()==n?me2.getN2():me2.getN1();
				//is there an edge between n1 and n2?
				if( g.getEdge(n1, n2) == null && g.getEdge(n2, n1) == null ) continue;
				//remove longest matching edge
				Edge meToRemove = me1.getGeometry().getLength() > me2.getGeometry().getLength() ? me1 : me2;
				mes.remove(meToRemove); g.remove(meToRemove);
			}
		}

	}




	private void extendSectionswithMatchingEdges() {

		for(Edge me : mes) {

			//get candidate section to extend
			Node n1 = me.getN1(), n2 = me.getN2();

			//no way to extend an existing section: Create new section from matching edge.
			if(n1.getEdges().size()>2 && n2.getEdges().size()>2) {
				//create new section from matching edge
				Feature f = new Feature();
				f.setGeom(me.getGeometry());
				//f.getProperties().putAll(); //TODO add properties 'in common' with other incoming sections
				if(tagOutput) f.set("EM", "created");
				me.obj = f;
				secs.add(f);
				continue;
			}

			//get section to extend
			Feature sectionToExtend = null;
			if(n2.getEdges().size()>2)
				sectionToExtend = getNonMatchingEdgeFromPair(n1.getEdges());
			else if(n1.getEdges().size()>2)
				sectionToExtend = getNonMatchingEdgeFromPair(n2.getEdges());
			else {
				//get section with worst resolution
				Feature s1 = getNonMatchingEdgeFromPair(n1.getEdges());
				Feature s2 = getNonMatchingEdgeFromPair(n2.getEdges());
				double res1 = getResolution(s1.get(cntAtt).toString());
				double res2 = getResolution(s2.get(cntAtt).toString());
				sectionToExtend = res1<res2? s2 : s1;
			}

			//extend section
			LineString g = null;
			try {
				g = extendLineString((LineString)sectionToExtend.getGeom(), me);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}

			sectionToExtend.setGeom(g);
			if(tagOutput) sectionToExtend.set("EM", "extended");
		}
	}

	//among a pair of edges, get the feature of the one which is not a matching edge
	private static Feature getNonMatchingEdgeFromPair(Set<Edge> edgePair) {
		if(edgePair.size() != 2) {
			LOGGER.error("Unexpected number of edges when getSectionToExtend: "+edgePair.size()+". Should be 2.");
			return null;
		}
		Iterator<Edge> it = edgePair.iterator();
		Edge e1 = it.next(), e2 = it.next();
		if(e1.obj != null && e2.obj == null) return (Feature) e1.obj;
		if(e1.obj == null && e2.obj != null) return (Feature) e2.obj;
		LOGGER.warn("Problem in getNonMatchingEdgeFromPair");
		return null;
	}


	//Extend line from a segment. The segment is supposed to be an extention of the line.
	private static LineString extendLineString(LineString ls, Edge me) throws Exception {

		LineMerger lm = new LineMerger();
		lm.add(ls); lm.add(me.getGeometry());
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



	//get all matching edges linked to a node (thoses with a null object)
	/*private ArrayList<Edge> getMatchingEdges(Node n) {
		ArrayList<Edge> mes_ = new ArrayList<Edge>();
		for(Edge e : n.getEdges()) if(e.obj == null) mes_.add(e);
		return mes_;
	}*/

	/*/check if two linestrings are connected from at least 2 of their tips
	private static boolean areConnected(LineString ls1, LineString ls2) {
		Coordinate[] cs1 = ls1.getCoordinates(), cs2 = ls2.getCoordinates();
		if(cs1[0].distance(cs2[0]) == 0) return true;
		if(cs1[0].distance(cs2[cs2.length-1]) == 0) return true;
		if(cs1[cs1.length-1].distance(cs2[0]) == 0) return true;
		if(cs1[cs1.length-1].distance(cs2[cs2.length-1]) == 0) return true;
		return false;
	}*/

	//check if a node has edges from different countries (NB: edges with no country are ignored)
	/*private boolean connectsCountries(Node n) {
		String cnt = null;
		for(Edge e : n.getEdges()) {
			//case of the presence of a matching edge
			if(e.obj == null) continue;
			//set initial country
			if(cnt == null) {
				cnt = ((Feature)e.obj).get(cntAtt).toString();
				continue;
			}
			if( !((Feature)e.obj).get(cntAtt).toString().equals(cnt) )
				return true;
		}
		return false;
	}*/
	//check if a node has at least one matching edges, that is a edge with no country specified
	/*private boolean hasME(Node n) {
		for(Edge e : n.getEdges())
			if(e.obj == null) return true;
		return false;
	}*/

}
