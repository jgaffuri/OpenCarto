package org.opencarto.algo.graph;

import java.util.Collection;
import java.util.HashSet;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.Node;

/**
 * @author julien Gaffuri
 *
 */
public class GraphConnexComponents {

	public static Collection<Graph> get(Graph g) {
		Collection<Graph> ccs = new HashSet<Graph>();

		Collection<Node> ns = new HashSet<Node>(); ns.addAll(g.getNodes());
		Collection<Edge> es = new HashSet<Edge>(); es.addAll(g.getEdges());

		while(!ns.isEmpty()){
			Node seed=ns.iterator().next();
			Graph cc = get(g, seed, ns, es);
			ccs.add(cc);
		}

		return ccs;
	}

	//extract the larger connex graph from ns
	private static Graph get(Graph g_, Node seed, Collection<Node> ns, Collection<Edge> es) {
		ns.remove(seed);
		Graph g = new Graph();
		g.getNodes().add(seed);

		for(Edge e:seed.getOutEdges()){
			if(!es.contains(e)) continue;
			g.getEdges().add(e);
			es.remove(e);
			g = new GraphUnion().union(g, get(g_, e.getN2(),ns,es));
		}

		for(Edge e:seed.getInEdges()){
			if(!es.contains(e)) continue;
			g.getEdges().add(e);
			es.remove(e);
			g = new GraphUnion().union(g, get(g_, e.getN1(),ns,es));
		}

		return g;
	}

	//return the connex component with the maximum number of nodes
	public static Graph getMainNodeNb(Graph g) {
		Graph gM=null; int nb, maxNb=-1;
		for(Graph g_ : get(g)) {
			nb = g_.getNodes().size();
			if(nb<=maxNb) continue;
			maxNb=nb; gM=g_;
		}
		return gM;
	}

	//print number of nodes of largest graphs
	public static void printNodeNb(Collection<Graph> ccs, int threshold) {
		int nb;
		for(Graph g : ccs) {
			nb = g.getNodes().size();
			if(nb < threshold) continue;
			System.out.println(nb);
		}
	}

}
