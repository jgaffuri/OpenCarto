package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashSet;

/**
 * Valued and oriented graph.
 * 
 * @author julien Gaffuri
 *
 */
public class Graph<T> {

	//the nodes
	private Collection<Node<T>> nodes = new HashSet<Node<T>>();
	public Collection<Node<T>> getNodes() { return nodes; }

	//build a node
	public Node<T> buildNode(){
		Node<T> n = new Node<T>();
		nodes.add(n);
		return n;
	}


	//the edges
	private Collection<Edge<T>> edges = new HashSet<Edge<T>>();
	public Collection<Edge<T>> getEdges() { return edges; }

	//build an edge
	public Edge<T> buildEdge(Node<T> n1, Node<T> n2){
		Edge<T> e = new Edge<T>(n1,n2);
		edges.add(e);
		return e;
	}



	//the domains
	private Collection<Domain<T>> domains = new HashSet<Domain<T>>();
	public Collection<Domain<T>> getDomains() { return domains; }

	//build a domain
	//TODO



	public void remove(Edge<T> e) {
		boolean b;
		b = edges.remove(e);
		if(!b) System.out.println("Error when removing edge (1) "+e);
		b = e.getN1().getOutEdges().remove(e);
		if(!b) System.out.println("Error when removing edge (2) "+e);
		b = e.getN2().getInEdges().remove(e);
		if(!b) System.out.println("Error when removing edge (3) "+e);
	}
	public void removeAll(Collection<Edge<T>> es) { for(Edge<T> e:es) remove(e); }

}
