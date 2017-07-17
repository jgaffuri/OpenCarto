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
	private Quadtree spInd = new Quadtree();

	//the nodes
	private Collection<Node> nodes = new HashSet<Node>();
	public Collection<Node> getNodes() { return nodes; }

	//build a node
	public Node buildNode(Coordinate c){
		Node n = new Node(c);
		nodes.add(n);
		spInd.insert(new Envelope(c), n);
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
		spInd.insert(e.getGeometry().getEnvelopeInternal(), e);
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


	public Node getNodeAt(Coordinate c) {
		Envelope env = new Envelope(c);
		//env.expandBy(5);
		List<?> elts = spInd.query(env);
		//System.out.println("--------");
		//System.out.println(c);
		//System.out.println(env);
		//System.out.println("nb="+elts.size());
		//System.out.println( spInd.depth() );
		//System.out.println( spInd.size() );
		for(Object elt : elts){
			if(!(elt instanceof Node)) continue;
			Node n = (Node)elt;
			if(c.distance(n.c) == 0) {
				//TODO bug - it never goes there!
				System.out.println("found!");
				return n;
			}
		}
		return null;
	}

	public Collection<Edge> getEdgesAt(Envelope env) {
		Collection<Edge> es = new HashSet<Edge>();
		List<?> es_ = spInd.query(env);
		for(Object o : es_) if(o instanceof Edge) es.add((Edge)o);
		return es;
	}

}
