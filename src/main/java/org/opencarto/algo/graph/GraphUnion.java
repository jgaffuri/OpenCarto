package org.opencarto.algo.graph;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.Node;


public class GraphUnion<T> {

	public Graph<T> union(Graph<T> g1, Graph<T> g2) {
		Graph<T> g = new Graph<T>();
		g.getNodes().addAll(g1.getNodes());
		g.getNodes().addAll(g2.getNodes());
		g.getEdges().addAll(g1.getEdges());
		g.getEdges().addAll(g2.getEdges());
		return g;
	}
	
	public Graph<T> aggregate(Graph<T> g1, Graph<T> g2, Node<T> n1, Node<T> n2, double edgeValue) {
		Graph<T> gAg = union(g1, g2);
		Edge<T> e = gAg.buildEdge(n1, n2);
		e.value=edgeValue;
		return gAg;
	}

}
