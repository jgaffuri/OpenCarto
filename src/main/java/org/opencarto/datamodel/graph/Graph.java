package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.quadtree.Quadtree;

/**
 * Valued and oriented graph.
 * 
 * @author julien Gaffuri
 *
 */
public class Graph{

	//the nodes
	private Collection<Node> nodes = new HashSet<Node>();
	public Collection<Node> getNodes() { return nodes; }

	//build a node
	public Node buildNode(Coordinate c){
		Node n = new Node(this,c);
		nodes.add(n);
		spIndNode.insert(new Envelope(n.getC()), n);
		return n;
	}


	//the edges
	private Collection<Edge> edges = new HashSet<Edge>();
	public Collection<Edge> getEdges() { return edges; }

	//build an edge
	public Edge buildEdge(Node n1, Node n2){ return buildEdge(n1,n2,null); }
	public Edge buildEdge(Node n1, Node n2, Coordinate[] coords){
		Edge e = new Edge(this, n1,n2,coords);
		edges.add(e);
		spIndEdge.insert(e.getGeometry().getEnvelopeInternal(), e);
		return e;
	}



	//the domains
	private Collection<Domain> domains = new HashSet<Domain>();
	public Collection<Domain> getDomains() { return domains; }

	//build a domain
	public Domain buildDomain() {
		Domain d = new Domain(this);
		domains.add(d);
		return d;
	}



	public void remove(Node n) {
		boolean b;
		b = nodes.remove(n);
		if(!b) System.err.println("Error when removing node "+n.getId()+". Not in graph nodes list.");
		if(n.getEdges().size()>0) System.err.println("Error when removing node "+n.getId()+". Edges are still linked to it (nb="+n.getEdges().size()+")");
		if(n.getDomains().size()>0) System.err.println("Error when removing node "+n.getId()+". Domains are still linked to it (nb="+n.getDomains().size()+")");
	}

	public void remove(Edge e) {
		boolean b;
		b = edges.remove(e);
		if(!b) System.err.println("Error when removing edge "+e.getId()+". Not in graph edges list.");
		b = e.getN1().getOutEdges().remove(e);
		if(!b) System.err.println("Error when removing edge (2) "+e.getId());
		b = e.getN2().getInEdges().remove(e);
		if(!b) System.err.println("Error when removing edge (3) "+e.getId());
		if(e.d1 != null) System.err.println("Error when removing edge "+e.getId()+". It is still linked to domain "+e.d1);
		if(e.d2 != null) System.err.println("Error when removing edge "+e.getId()+". It is still linked to domain "+e.d2);
	}
	public void removeAll(Collection<Edge> es) { for(Edge e:es) remove(e); }


	public void removeDomain(Domain dom) {
		boolean b;

		//remove domain from list
		b = getDomains().remove(dom);
		if(!b) System.err.println("Could not remove domain "+dom.getId()+" from graph");

		//break link with edges
		for(Edge e:dom.getEdges()){
			if(e.d1==dom) e.d1=null;
			else if(e.d2==dom) e.d2=null;
			else System.err.println("Could not remove link between domain "+dom.getId()+" and edge "+e.getId());
		}

	}


	//support for spatial queries

	private SpatialIndex spIndNode = new Quadtree();
	public SpatialIndex getSpatialIndexNode() { return spIndNode; }
	private SpatialIndex spIndEdge = new Quadtree();
	public SpatialIndex getSpatialIndexEdge() { return spIndEdge; }

	/*public Quadtree getNodeSpatialIndex(){
		Quadtree si = new Quadtree();
		for(Node n : getNodes()) si.insert(new Envelope(n.getC()), n);
		return si;
	}*/

	public Node getNodeAt(Coordinate c) {
		Envelope env = new Envelope(c);
		//env.expandBy(5);
		List<?> elts = spIndNode.query(env);
		for(Object elt : elts){
			Node n = (Node)elt;
			if(c.distance(n.getC()) == 0) return n;
		}
		return null;
	}


	/*public Quadtree getEdgeSpatialIndex(){
		Quadtree si = new Quadtree();
		for(Edge e : getEdges()) si.insert(e.getGeometry().getEnvelopeInternal(), e);
		return si;
	}*/

	public List<Edge> getEdgesAt(Envelope env) {
		return spIndEdge.query(env);
	}

}
