/**
 * 
 */
package org.opencarto.algo.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.opencarto.algo.base.Union;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.Node;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author julien Gaffuri
 *
 */
public class StrokeAnalysis {
	public final static Logger LOGGER = Logger.getLogger(StrokeAnalysis.class.getName());

	private Graph g = null;

	public StrokeAnalysis(Graph g) { this.g = g; }

	private Collection<Stroke> strokes;
	public Collection<Stroke> getStrokes() { return strokes; }

	public class Stroke extends Feature {
		private List<Feature> sections = new ArrayList<>();
		public List<Feature> getSections() { return sections; }

		public Stroke(StrokeC s) {
			//set list of features
			for(Edge e : s.edges) sections.add( (Feature)e.obj );
			//build and set geometry
			Collection<Geometry> gs = new ArrayList<Geometry>();
			for(Edge e : s.edges) gs.add(e.getGeometry());
			this.setGeom( Union.getUnionAsLineString(gs) );
			//set salience
			set("length",getGeom().getLength());
		}
	}

	public StrokeAnalysis run(double minSal) {

		if(LOGGER.isTraceEnabled()) LOGGER.trace("build initial list of strokes with single edges");
		Collection<StrokeC> sts = new ArrayList<>();
		for(Edge e: g.getEdges()) {
			StrokeC s = new StrokeC();
			s.edges.add(e);
			sts.add(s);
		}

		//get list of possible connections and index it by node
		ArrayList<StrokeConnection> cs = getPossibleConnections(sts, minSal);

		//merge strokes iterativelly
		while( !cs.isEmpty() )
			merge(cs.get(0), sts, cs);

		//build final strokes
		strokes = new ArrayList<>();
		for(StrokeC s_ : sts) strokes.add(new Stroke(s_));

		return this;
	}


	/*
	private HashMap<Node, Collection<StrokeConnection>> indexStrokeConnectionByNode(ArrayList<StrokeConnection> cs) {
		HashMap<Node, Collection<StrokeConnection>> csI = new HashMap<>();
		for(StrokeConnection c : cs) {
			Collection<StrokeConnection> csss = csI.get(c.n);
			if(csss == null) {
				csss = new ArrayList<StrokeConnection>();
				csI.put(c.n, csss);
			}
			csss.add(c);
		}
		return csI;
	}
	 */



	//for the computation only

	private class StrokeC {
		Collection<Edge> edges = new ArrayList<>();
		/*public double getLength() {
			double len = 0; for(Edge e : edges) len+=e.getGeometry().getLength(); return len;
		}*/

		public boolean isClosed() {
			Set<Node> s = new HashSet<>();
			for(Edge e : edges) {
				if(s.contains(e.getN1())) s.remove(e.getN1()); else s.add(e.getN1());
				if(s.contains(e.getN2())) s.remove(e.getN2()); else s.add(e.getN2());
			}
			if(s.size() == 0) return true;
			if(s.size() != 2) LOGGER.warn("Problem when measuring if stroke is closed. s="+s.size());;
			return false;
		}
	}

	public class StrokeConnection {
		Node n;
		Edge e1, e2;
		StrokeC s1, s2;
		double sal;
		StrokeConnection(Node n, Edge e1, Edge e2, StrokeC s1, StrokeC s2) {
			this.n=n;
			this.e1=e1; this.e2=e2;
			this.s1=s1; this.s2=s2;
			//compute salience
			Coordinate c = n.getC();
			Coordinate c1 = getCoordinateForDeflation(e1,n);
			Coordinate c2 = getCoordinateForDeflation(e2,n);
			double ang = Angle.angleBetween(c1, c, c2);
			//ang between 0 and Pi
			if(ang<0 || ang>Math.PI) {
				LOGGER.warn("Unexpected deflection angle value around "+c+". Should be within [0,Pi]. "+ang);
			}
			sal = ang/Math.PI;

			//TODO compute salience based on deflection angle + attributes of feature + other? length?
			//TODO get criteria from article - or generalisation algorithm
		}
		private Coordinate getCoordinateForDeflation(Edge e, Node n) {
			Coordinate c = null;
			Coordinate[] cs = e.getCoords();
			if(n.getC().distance(cs[0]) == 0)
				c = cs[1];
			else if(n.getC().distance(cs[cs.length-1]) == 0)
				c = cs[cs.length-2];
			else
				LOGGER.warn("Could not getCoordinateForDeflation around "+n.getC());
			return c;
		}
	}


	//get all possible connections, which have a deflection angle smaller than a max value
	//return a list sorted by salience
	private ArrayList<StrokeConnection> getPossibleConnections(Collection<StrokeC> sts, double minSal) {

		//index strokes by edge
		HashMap<Edge,StrokeC> ind = new HashMap<Edge,StrokeC>();
		for(StrokeC s : sts) ind.put(s.edges.iterator().next(), s);

		//build possible connections
		ArrayList<StrokeConnection> cs = new ArrayList<>();
		Edge ei, ej;
		for(Node n : g.getNodes()) {
			ArrayList<Edge> es = n.getEdgesAsList();
			for(int i=0; i<es.size(); i++) {
				ei = es.get(i);
				for(int j=i+1; j<es.size(); j++) {
					ej = es.get(j);
					StrokeConnection sc = new StrokeConnection(n, ei, ej, ind.get(ei), ind.get(ej));
					if(sc.sal>=minSal) cs.add(sc);
				}
			}
		}

		//sort cs by salience, starting with the the higest value
		cs.sort(new Comparator<StrokeConnection>() {
			@Override
			public int compare(StrokeConnection sc0, StrokeConnection sc1) { return (int)(1e12*(sc1.sal-sc0.sal)); }
		});
		return cs;
	}

	//merge two connected strokes 
	private void merge(StrokeConnection c, Collection<StrokeC> sts, Collection<StrokeConnection> cs/*, Collection<StrokeConnection> csn*/) {
		boolean b;

		//handle case when closed edge
		if(c.s1 == c.s2) {
			LOGGER.info("Loop! "+c.n.getC());
			removeStrokeConnections(c,c.s1,cs);
			return;
		}

		if(c.s1.isClosed()) {
			LOGGER.info("Loop! "+c.n.getC());
			removeStrokeConnections(c,c.s1,cs);
			return;
		}
		if(c.s2.isClosed()) {
			LOGGER.info("Loop! "+c.n.getC());
			removeStrokeConnections(c,c.s2,cs);
			return;
		}

		//make new stroke
		StrokeC sNew = new StrokeC();
		sNew.edges.addAll(c.s1.edges);
		sNew.edges.addAll(c.s2.edges);

		//add it to list
		b = sts.add(sNew);
		if(!b) LOGGER.warn("Problem when merging strokes. Could not add merged stroke to list.");

		//remove merged strokes
		b = sts.remove(c.s1);
		if(!b) LOGGER.warn("Problem when merging strokes. Could not remove stroke from list.");
		b = sts.remove(c.s2);
		if(!b) LOGGER.warn("Problem when merging strokes. Could not remove stroke from list.");

		//remove stroke connections at c.n, which are linked to either c.s1 or c.s2 (or both)
		//removeStrokeConnections(c,c.s1,cs);
		//removeStrokeConnections(c,c.s2,cs);
		ArrayList<StrokeConnection> csToRemove = new ArrayList<>();
		for(StrokeConnection ccc : cs) {
			if(ccc.n != c.n) continue;
			if(ccc.s1==c.s1 || ccc.s1==c.s2 || ccc.s2==c.s1 || ccc.s2==c.s2) csToRemove.add(ccc);
		}
		b = cs.removeAll(csToRemove);
		if(!b) LOGGER.warn("Problem when merging strokes. Could not remove connections from list.");

		//update references to connections
		for(StrokeConnection ccc : cs) {
			if(ccc.n == c.n) continue;
			if(ccc.s1 == c.s1 || ccc.s1 == c.s2) ccc.s1 = sNew;
			if(ccc.s2 == c.s1 || ccc.s2 == c.s2) ccc.s2 = sNew;
		}

	}



	private void removeStrokeConnections(StrokeConnection c, StrokeC s, Collection<StrokeConnection> cs) {
		ArrayList<StrokeConnection> csToRemove = new ArrayList<>();
		for(StrokeConnection ccc : cs) {
			if(c!=null && ccc.n != c.n) continue;
			if(ccc.s2==s || ccc.s2==s) csToRemove.add(ccc);
		}
		boolean b = cs.removeAll(csToRemove);
		if(!b) LOGGER.warn("Problem when merging strokes. Could not remove connections from list.");
	}


}
