/**
 * 
 */
package org.opencarto.algo.graph.stroke;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.opencarto.algo.graph.GraphConnexComponents;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.GraphBuilder;
import org.opencarto.datamodel.graph.Node;

/**
 * @author julien Gaffuri
 *
 */
public class StrokeAnalysis {
	public final static Logger LOGGER = Logger.getLogger(StrokeAnalysis.class.getName());

	private Graph g = null;
	private StrokeConnectionSalienceComputation sco = new StrokeConnectionSalienceComputation();
	public StrokeAnalysis setSco(StrokeConnectionSalienceComputation sco) { this.sco = sco; return this; }
	private StrokeSalienceComputation ssco = new StrokeSalienceComputation();
	public StrokeAnalysis setSco(StrokeSalienceComputation ssco) { this.ssco = ssco; return this; }

	public StrokeAnalysis(Graph g) { this.g = g; }

	public StrokeAnalysis(Collection<Feature> fs, boolean keepOnlyMainGraphComponent) {

		//build graph
		g = GraphBuilder.buildForNetworkFromLinearFeaturesNonPlanar(fs);
		//TODO fix and use that:
		//g = GraphBuilder.buildForNetworkFromLinearFeatures(fs);

		//keep only main component
		if(keepOnlyMainGraphComponent)
			g = GraphConnexComponents.getMainNodeNb(g);

	}


	//the output
	private Collection<Stroke> strokes;
	public Collection<Stroke> getStrokes() { return strokes; }


	public StrokeAnalysis run(double minSal) {

		//build initial list of strokes with single edges
		Collection<StrokeC> sts = getInitialStrokeCs();

		//get list of possible connections and index it by node
		ArrayList<StrokeConnection> cs = getPossibleConnections(sts, minSal);

		//merge strokes iterativelly
		while( !cs.isEmpty() )
			merge(cs.get(0), sts, cs);

		//build final strokes
		strokes = new ArrayList<>();
		for(StrokeC s_ : sts) strokes.add(new Stroke(s_.edges));

		//compute stoke salience
		ssco.setSalience(strokes);

		return this;
	}


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

	private class StrokeConnection {
		Node n;
		StrokeC s1, s2;
		double sal;
		StrokeConnection(Node n, Edge e1, Edge e2, StrokeC s1, StrokeC s2, StrokeConnectionSalienceComputation sco) {
			this.n=n;
			this.s1=s1; this.s2=s2;
			this.sal = sco.computeSalience(n,e1,e2);
		}
	}


	//make initial strokes. Group edges having the same obj
	private Collection<StrokeC> getInitialStrokeCs() {
		//index edges by object
		HashMap<Object,Collection<Edge>> index = new HashMap<Object,Collection<Edge>>();
		for(Edge e : g.getEdges()) {
			Collection<Edge> es = index.get(e.obj);
			if(es == null) {
				es = new ArrayList<Edge>();
				index.put(e.obj, es);
			}
			es.add(e);
		}

		Collection<StrokeC> sts = new ArrayList<>();
		for(Collection<Edge> e : index.values()) {
			StrokeC s = new StrokeC();
			s.edges.addAll(e);
			sts.add(s);
		}
		return sts;
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
					StrokeConnection sc = new StrokeConnection(n, ei, ej, ind.get(ei), ind.get(ej), sco);
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
	private void merge(StrokeConnection c, Collection<StrokeC> sts, Collection<StrokeConnection> cs) {
		boolean b;

		//handle case when closed edge
		if(c.s1 == c.s2) {
			LOGGER.info("Loop 1! "+c.n.getC());
			//TODO check that
			removeStrokeConnections(c,c.s1,cs);
			return;
		}

		if(c.s1.isClosed()) {
			LOGGER.info("Loop 2! "+c.n.getC());
			//TODO check that
			removeStrokeConnections(c,c.s1,cs);
			return;
		}
		if(c.s2.isClosed()) {
			LOGGER.info("Loop 3! "+c.n.getC());
			//TODO check that
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
		//TODO fix that
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
