package org.opencarto.algo.graph;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.Node;


public class GraphUnion {

	public Graph union(Graph g1, Graph g2) {
		Graph g = new Graph();
		g.getNodes().addAll(g1.getNodes());
		g.getNodes().addAll(g2.getNodes());
		g.getEdges().addAll(g1.getEdges());
		g.getEdges().addAll(g2.getEdges());
		return g;
	}
	
	public Graph aggregate(Graph g1, Graph g2, Node n1, Node n2, double edgeValue) {
		Graph gAg = union(g1, g2);
		Edge e = gAg.buildEdge(n1, n2);
		e.value=edgeValue;
		return gAg;
	}

}
