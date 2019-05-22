/**
 * 
 */
package org.opencarto.algo.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.Node;

/**
 * @author julien Gaffuri
 *
 */
public class NodeReduction {
	//private final static Logger LOGGER = Logger.getLogger(NodeReduction.class.getName());

	//ensure a node degree is not 2. If it is, merge the two edges.
	//returns the deleted edge
	public static Edge ensureReduction(Node n) {
		Collection<Edge> es = n.getEdges();
		if(es.size()!=2) return null;
		Iterator<Edge> it = es.iterator();
		Edge e1 = it.next(), e2 = it.next();
		if(TopologyAnalysis.isClosed(e1) || TopologyAnalysis.isClosed(e2)) return null;
		return EdgeMerging.merge(n.getGraph(), e1, e2);
	}

	//ensure reduction of all nodes in the graph
	//return the deleted edges
	public static Collection<Edge> ensureNodeReduction(Graph g) {
		Collection<Edge> out = new ArrayList<>();
		for(Node n : g.getNodes()) {
			Edge e = ensureReduction(n);
			if(e != null) out.add(e);
		}
		return out;
	}

}
