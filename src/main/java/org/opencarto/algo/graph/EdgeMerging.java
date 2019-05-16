/**
 * 
 */
package org.opencarto.algo.graph;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.Node;

/**
 * @author julien Gaffuri
 *
 */
public class EdgeMerging {
	private final static Logger LOGGER = Logger.getLogger(EdgeMerging.class.getName());

	//merge two edges into a new single one
	public static Edge merge(Graph g, Edge e1, Edge e2) {
		if(TopologyAnalysis.isClosed(e1) || TopologyAnalysis.isClosed(e2)){
			LOGGER.error("Cannot merge edges if one of them is closed.");
			return null;
		}

		//"closed" case
		if(e1.getN1()==e2.getN1() && e1.getN2()==e2.getN2()) return merge(g, GraphUtils.revert(e1), e2);
		//handle other cases
		else if(e1.getN1()==e2.getN2() && e1.getN2()!=e2.getN1()) return merge(g, e2, e1);
		else if(e1.getN1()==e2.getN1()) return merge(g, GraphUtils.revert(e1),e2);
		else if(e1.getN2()==e2.getN2()) return merge(g, e1, GraphUtils.revert(e2));

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
		e1.setGeom(coords);
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
		if(TopologyAnalysis.isClosed(e1) || TopologyAnalysis.isClosed(e2)) return null;
		return merge(n.getGraph(), e1, e2);
	}


}
