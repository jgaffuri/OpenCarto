/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.Collection;

import org.opencarto.datamodel.graph.Domain;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.Node;
import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.Transformation;

/**
 * 
 * Delete a graph domain. Do not handle case of amalgamation.
 * It should be used only to remove island domains.
 * This operation may result in a hole in the graph tesselation. To prevent that, use aggregation instead.
 * The edges and nodes which are not linked anymore to any other graph element are also deleted
 * 
 * @author julien Gaffuri
 * 
 */
public class TDomainDeletion extends Transformation {

	@Override
	public void apply(Agent agent) {
		System.out.println("Delete "+agent.getId());

		ADomain domAg = (ADomain)agent;
		domAg.setDeleted(true);

		Domain dom = domAg.getObject();
		Graph g = dom.getGraph();

		g.removeDomain(dom);

		//remove useless edges
		Collection<Edge> es = dom.getEdges();
		for(Edge e:es){
			if(e.getDomains().size()>0) continue;
			g.remove(e);
		}

		//remove useless nodes
		Collection<Node> ns = dom.getNodes();
		for(Node n:ns){
			if(n.getDomains().size()>0) continue;
			g.remove(n);
		}
	}
}
