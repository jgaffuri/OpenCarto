/**
 * 
 */
package org.opencarto.algo.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Node;

/**
 * @author julien Gaffuri
 *
 */
public class NodeReduction {
	//private final static Logger LOGGER = Logger.getLogger(NodeReduction.class.getName());


	//specify when a node can be reduced or not
	interface NodeReductionCriteria {
		public boolean isReducable(Node n);
	}

	//the default case: 2 edges that are not closed
	private static class DefaultNodeReductionCriteria implements NodeReductionCriteria {
		@Override
		public boolean isReducable(Node n) {
			Collection<Edge> es = n.getEdges();
			if(es.size()!=2) return false;
			Iterator<Edge> it = es.iterator();
			Edge e1 = it.next(), e2 = it.next();
			if(TopologyAnalysis.isClosed(e1) || TopologyAnalysis.isClosed(e2)) return false;
			return true;
		}
	}

	public static NodeReductionCriteria DEFAULT_NODE_REDUCTION_CRITERIA = new DefaultNodeReductionCriteria();




	//ensure a node degree is not 2. If it is, merge the two edges.
	//returns the deleted edge
	public static Edge ensure(Node n, NodeReductionCriteria nrc) {
		if(! nrc.isReducable(n)) return null;
		Iterator<Edge> it = n.getEdges().iterator();
		Edge e1 = it.next(), e2 = it.next();
		return EdgeMerging.merge(n.getGraph(), e1, e2);
	}
	public static Edge ensure(Node n) {
		return ensure(n, DEFAULT_NODE_REDUCTION_CRITERIA);
	}

	//ensure reduction of all nodes in the graph
	//return the deleted edges
	public static Collection<Edge> ensure(Collection<Node> ns, NodeReductionCriteria nrc) {
		Collection<Edge> out = new ArrayList<>();
		for(Node n : ns) {
			Edge e = ensure(n, nrc);
			if(e != null) out.add(e);
		}
		return out;
	}
	public static Collection<Edge> ensure(Collection<Node> ns) {
		return ensure(ns, DEFAULT_NODE_REDUCTION_CRITERIA);
	}


}
