/**
 * 
 */
package org.opencarto.algo.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.Node;

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

		public Stroke(Stroke_ s) {
			//set features
			for(Edge e : s.edges) sections.add( (Feature)e.obj );
			//set salience
			this.set("s", s.getSalience());
			//build and set geometry TODO: use linemerger?
			Geometry g = null;
			for(Edge e : s.edges) g = g==null? e.getGeometry() : g.union(e.getGeometry());
			this.setGeom(g);
		}
	}



	public StrokeAnalysis run(double maxDefletionAngleDeg) {

		//build initial list of strokes with single edges
		Collection<Stroke_> sts = new ArrayList<>();
		for(Edge e: g.getEdges()) sts.add(new Stroke_(e));

		//get list of possible connections
		ArrayList<StrokeConnection> cs = getPossibleConnections(sts, maxDefletionAngleDeg);

		//merge strokes iterativelly
		while( !cs.isEmpty() )
			merge(cs.get(0), sts, cs);

		//build final strokes
		strokes = new ArrayList<>();
		for(Stroke_ s_ : sts) strokes.add(new Stroke(s_));

		return this;
	}


	//for the computation only

	private class Stroke_ {
		Stroke_(Edge e) { edges.add(e); }
		List<Edge> edges = new ArrayList<>();
		double getSalience() {
			//TODO depends on length ?
			return -1;
		}
	}

	public class StrokeConnection {
		Node n;
		Edge e1, e2;
		Stroke_ s1, s2;
		double defletionAngleDeg, sal;
		StrokeConnection(Node n, Edge e1, Edge e2, Stroke_ s1, Stroke_ s2) {
			this.n=n;
			this.e1=e1; this.e2=e2;
			this.s1=s1; this.s2=s2;
			//TODO compute deflection angle in degree + salience depending on attributes of feature
		}
	}


	//get all possible connections, which have a deflection angle smaller than a max value
	//return a list sorted by salience
	private ArrayList<StrokeConnection> getPossibleConnections(Collection<Stroke_> sts, double maxDefletionAngleDeg) {

		//index strokes by edge
		HashMap<Edge,Stroke_> ind = new HashMap<Edge,Stroke_>();
		for(Stroke_ s : sts) ind.put(s.edges.get(0), s);

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
					if(sc.sal<=maxDefletionAngleDeg) cs.add(sc);
				}
			}
		}

		//sort cs by salience, starting with the the higest value
		cs.sort(new Comparator<StrokeConnection>() {
			@Override
			public int compare(StrokeConnection sc0, StrokeConnection sc1) { return (int)(1000000*(sc0.sal-sc1.sal)); }
		});
		return cs;
	}

	//merge two connected strokes 
	private void merge(StrokeConnection c, Collection<Stroke_> sts, Collection<StrokeConnection> cs) {
		boolean b;

		//TODO make a new stroke from c.s1 and c.s2
		Stroke_ sNew = null;

		//update sts
		b = sts.add(sNew);
		if(!b) LOGGER.warn("Problem when merging strokes. Could not add merged stroke to list.");
		b = sts.remove(c.s1);
		if(!b) LOGGER.warn("Problem when merging strokes. Could not remove stroke from list.");
		b = sts.remove(c.s2);
		if(!b) LOGGER.warn("Problem when merging strokes. Could not remove stroke from list.");

		//update cs
		b = cs.remove(c);
		if(!b) LOGGER.warn("Problem when merging strokes. Could not remove connection from list.");
		//TODO remove also connections around c.n, which are linked either c.s1 or c.s2
	}

}
