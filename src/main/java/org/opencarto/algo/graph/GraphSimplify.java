/**
 * 
 */
package org.opencarto.algo.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.Node;

/**
 * @author julien Gaffuri
 *
 */
public class GraphSimplify {
	private final static Logger LOGGER = Logger.getLogger(GraphSimplify.class.getName());


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








	//reverse the edge
	public static Edge revert(Edge e) {
		//revert geometry
		Coordinate[] cs = e.getCoords();
		e.coords = new Coordinate[cs.length];
		for(int i=0;i<cs.length;i++) e.coords[i]=cs[cs.length-1-i];
		cs = null;
		//Revert nodes
		/*boolean b;
		b = n1.getOutEdges().remove(this);
		if(!b) LOGGER.severe("Error (1) in revert of "+getId());
		b = n1.getInEdges().add(this);
		if(!b) LOGGER.severe("Error (2) in revert of "+getId());
		b = n2.getInEdges().remove(this);
		if(!b) LOGGER.severe("Error (3) in revert of "+getId());
		b = n2.getOutEdges().add(this);
		if(!b) LOGGER.severe("Error (4) in revert of "+getId());*/
		Node n=e.getN1(); e.setN1(e.getN2()); e.setN2(n);
		return e;
	}

	//merge two edges into a new single one
	public static Edge merge(Graph g, Edge e1, Edge e2) {
		if(e1.isClosed() || e2.isClosed()){
			LOGGER.error("Cannot merge edges if one of them is closed.");
			return null;
		}

		//"closed" case
		if(e1.getN1()==e2.getN1() && e1.getN2()==e2.getN2()) return merge(g, revert(e1),e2);
		//handle other cases
		else if(e1.getN1()==e2.getN2() && e1.getN2()!=e2.getN1()) return merge(g, e2,e1);
		else if(e1.getN1()==e2.getN1()) return merge(g, revert(e1),e2);
		else if(e1.getN2()==e2.getN2()) return merge(g, e1,revert(e2));

		//get nodes
		Node n=e1.getN2(), n2=e2.getN2();

		LOGGER.debug("merge around "+n.getId() +" "+ n.getC());

		//build new edge geometry
		int nb1 = e1.getCoords().length, nb2 = e2.getCoords().length;
		Coordinate[] coords = new Coordinate[nb1+nb2-1];
		for(int i=0; i<nb1; i++) coords[i] = e1.getCoords()[i];
		for(int i=nb1; i<nb1+nb2-1; i++) coords[i] = e2.getCoords()[i-nb1+1];

		//disconnect and remove e2
		if(e2.f1!=null) { e2.f1.getEdges().remove(e2); e2.f1=null; }
		if(e2.f2!=null) { e2.f2.getEdges().remove(e2); e2.f2=null; }
		g.remove(e2);

		//update e1 with new geometry and new final node
		e1.setGeom(new GeometryFactory().createLineString(coords));
		e1.setN2(n2);

		//remove middle node
		g.remove(n);

		return e2;
	}


	//ensure a node degree is not 2. If it is, merge the two edges.
	//returns the deleted edge
	public static Edge ensureReduction(Node n) {
		Collection<Edge> es = n.getEdges();
		if(es.size()!=2) return null;
		Iterator<Edge> it = es.iterator();
		Edge e1=it.next(), e2=it.next();
		if(e1.isClosed() || e2.isClosed()) return null;
		return merge(n.getGraph(),e1,e2);
	}

}