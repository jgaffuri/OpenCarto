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
public class GraphConnexComponents<T> {

	public Collection<Graph<T>> getConnexComponents(Graph<T> g) {
		Collection<Graph<T>> ccs = new HashSet<Graph<T>>();

		Collection<Node<T>> ns = new HashSet<Node<T>>(); ns.addAll(g.getNodes());
		Collection<Edge<T>> es = new HashSet<Edge<T>>(); es.addAll(g.getEdges());

		while(!ns.isEmpty()){
			Node<T> seed=ns.iterator().next();
			Graph<T> cc = getConnexComponent(g, seed, ns, es);
			ccs.add(cc);
		}

		return ccs;
	}

	//extract the larger connex graph from ns
	private Graph<T> getConnexComponent(Graph<T> g_, Node<T> seed, Collection<Node<T>> ns, Collection<Edge<T>> es) {
		ns.remove(seed);
		Graph<T> g = new Graph<T>();
		g.getNodes().add(seed);

		for(Edge<T> e:seed.getOutEdges()){
			if(!es.contains(e)) continue;
			g.getEdges().add(e);
			es.remove(e);
			g = new GraphUnion<T>().union(g, getConnexComponent(g_, e.getN2(),ns,es));
		}

		for(Edge<T> e:seed.getInEdges()){
			if(!es.contains(e)) continue;
			g.getEdges().add(e);
			es.remove(e);
			g = new GraphUnion<T>().union(g, getConnexComponent(g_, e.getN1(),ns,es));
		}

		return g;
	}

}
