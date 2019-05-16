/**
 * 
 */
package org.opencarto.algo.graph;

import java.util.HashSet;
import java.util.Set;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.Node;

/**
 * @author julien Gaffuri
 *
 */
public class EdgeCollapse {
	//private final static Logger LOGGER = Logger.getLogger(EdgeCollapse.class.getName());


	//both nodes are collapsed to the center of the edge
	public static void collapseEdge(Edge e) {
		Graph g = e.getGraph();

		//get nodes
		Node n1 = e.getN1(), n2 = e.getN2();

		//break link edge/faces
		if(e.f1 != null) { e.f1.getEdges().remove(e); e.f1=null; }
		if(e.f2 != null) { e.f2.getEdges().remove(e); e.f2=null; }

		//remove edge
		g.remove(e);

		//move n1 to edge center
		NodeDisplacement.moveTo( n1, 0.5*(n1.getC().x+n2.getC().x), 0.5*(n1.getC().y+n2.getC().y) );

		//make n1 origin of all edges starting from node n2
		Set<Edge> es;
		es = new HashSet<Edge>(); es.addAll(n2.getOutEdges());
		for(Edge e_ : es) e_.setN1(n1);

		//make n1 destination of all edges going to n2
		es = new HashSet<Edge>(); es.addAll(n2.getInEdges());
		for(Edge e_ : es) e_.setN2(n1);

		//System.out.println(n2.getOutEdges().size() +"   "+ n2.getInEdges().size());

		//remove n2
		g.remove(n2);
	}

	//find one edge shorter than a threshold values
	public static Edge findTooShortEdge(Graph g, double d) {
		for(Edge e : g.getEdges())
			if(e.getGeometry().getLength() < d)
				return e;
		return null;
	}

	//collapse too short edges
	public static void collapseTooShortEdges(Graph g, double d) {
		Edge e = findTooShortEdge(g, d);
		while(e != null) {
			collapseEdge(e);
			e = findTooShortEdge(g, d);
		}
	}

}
