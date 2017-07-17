package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.quadtree.Quadtree;

/**
 * Valued and oriented graph.
 * 
 * @author julien Gaffuri
 *
 */
public class Graph{
	//used to search graph elements
	private Quadtree qt = new Quadtree();

	//the nodes
	private Collection<Node> nodes = new HashSet<Node>();
	public Collection<Node> getNodes() { return nodes; }

	//build a node
	public Node buildNode(Coordinate c){
		Node n = new Node(c);
		nodes.add(n);
		qt.insert(new Envelope(c), this);
		return n;
	}


	//the edges
	private Collection<Edge> edges = new HashSet<Edge>();
	public Collection<Edge> getEdges() { return edges; }

	//build an edge
	public Edge buildEdge(Node n1, Node n2){ return buildEdge(n1,n2,null); }
	public Edge buildEdge(Node n1, Node n2, Coordinate[] coords){
		Edge e = new Edge(n1,n2,coords);
		edges.add(e);
		qt.insert(e.getGeometry().getEnvelopeInternal(), this);
		return e;
	}



	//the domains
	private Collection<Domain> domains = new HashSet<Domain>();
	public Collection<Domain> getDomains() { return domains; }

	//build a domain
	public Domain buildDomain() {
		Domain d = new Domain();
		domains.add(d);
		return d;
	}



	public void remove(Edge e) {
		boolean b;
		b = edges.remove(e);
		if(!b) System.out.println("Error when removing edge (1) "+e);
		b = e.getN1().getOutEdges().remove(e);
		if(!b) System.out.println("Error when removing edge (2) "+e);
		b = e.getN2().getInEdges().remove(e);
		if(!b) System.out.println("Error when removing edge (3) "+e);
	}
	public void removeAll(Collection<Edge> es) { for(Edge e:es) remove(e); }


	public Node getNodeAt(Coordinate c0) {
		List<?> ns = qt.query(new Envelope(c0));
		for(Object o : ns){
			if(!(o instanceof Node)) continue;
			Node n = (Node)o;
			if(c0.distance(n.c) == 0) return n;
		}
		return null;
	}

	public Collection<Edge> getEdgesAt(Envelope env) {
		Collection<Edge> es = new HashSet<Edge>();
		List<?> es_ = qt.query(env);
		for(Object o : es_) if(o instanceof Edge) es.add((Edge)o);
		return es;
	}

}
