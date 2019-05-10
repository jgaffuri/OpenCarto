/**
 * 
 */
package org.opencarto.algo.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Face;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.Node;

/**
 * @author julien Gaffuri
 *
 */
public class FaceAggregation {
	private final static Logger LOGGER = Logger.getLogger(FaceAggregation.class.getName());


	//aggregate two faces
	//return the deleted edges
	public static Set<Edge> aggregate(Graph g, Face targetFace, Face delFace) {
		if(delFace==targetFace){
			LOGGER.error("Error: Cannot aggregate a face with itself.");
			return null;
		}

		//get edges to delete (the ones in common)
		Set<Edge> delEdges = targetFace.getEdgesInCommon(delFace);
		if(delEdges.size()==0){
			LOGGER.error("Could not aggregate face "+delFace.getId()+" with face "+targetFace.getId()+": No edge in common.");
			return delEdges;
		}

		boolean b = true;
		//TODO remove this special case maybe?
		if(delFace.isEnclave()){
			//store nodes, to remove them in the end
			Collection<Node> ns = delFace.getNodes();

			//remove face (making hole)
			g.remove(delFace);

			//remove hole - remove edges
			b = targetFace.getEdges().removeAll(delEdges);
			if(!b) LOGGER.error("Error when aggregating (enclave) face "+delFace.getId()+" into face "+targetFace.getId()+": Failed in removing edges of absorbed face "+delFace.getId());
			for(Edge e : delEdges){ e.f1=null; e.f2=null; g.remove(e); }

			//remove remaining nodes
			for(Node n:ns)
				if(n.getEdgeNumber()==0)
					g.remove(n);
		} else {
			//store nodes concerned
			Set<Node> nodes = new HashSet<Node>();
			for(Edge e : delEdges) { nodes.add(e.getN1()); nodes.add(e.getN2()); }

			//get edges to move from delFace from targetFace
			Set<Edge> moveEdge = new HashSet<Edge>();
			b = moveEdge.addAll(delFace.getEdges());
			b = moveEdge.removeAll(delEdges);
			if(moveEdge.size()+delEdges.size()!=delFace.getEdges().size()) LOGGER.error("Error when aggregating face "+delFace.getId()+" into face "+targetFace.getId()+": inconsistent sets");

			//remove face, leaving a hole
			g.remove(delFace);

			//remove hole - remove edges
			b = targetFace.getEdges().removeAll(delEdges);
			if(!b) LOGGER.error("Error when aggregating face "+delFace.getId()+" into face "+targetFace.getId()+": Failed in removing edges of absorbing face "+ targetFace.getId()+". Nb="+delEdges.size());
			for(Edge e : delEdges){ e.f1=null; e.f2=null; g.remove(e); }

			//link remaining edges from absorbed face to target face
			for(Edge e : moveEdge)
				if(e.f1==null) e.f1 = targetFace;
				else if(e.f2==null) e.f2 = targetFace;
				else LOGGER.error("Error when aggregating face "+delFace.getId()+" into face "+targetFace.getId()+": Edge "+e.getId()+" should be linked to null face but it is not. Linked to: "+e.f1+" and "+e.f2);
			b = targetFace.getEdges().addAll(moveEdge);
			if(!b) LOGGER.error("Error when aggregating face "+delFace.getId()+" into face "+targetFace.getId()+": Failed in adding new edges to absorbing face "+targetFace.getId());

			//remove single nodes
			for(Node n : nodes)
				if(n.getEdgeNumber()==0)
					g.remove(n);

			//ensure nodes are reduced, which means they do not have a degree 2
			for(Node n : nodes){
				Edge e = GraphSimplify.ensureReduction(n);
				if(e != null) delEdges.add(e);
			}
		}

		//force faces geometry update
		targetFace.updateGeometry();
		delFace.updateGeometry();

		return delEdges;
	}



}
