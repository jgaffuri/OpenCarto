/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.Collection;

import org.opencarto.datamodel.graph.Domain;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.Node;
import org.opencarto.transfoengine.Transformation;

/**
 * 
 * @author julien Gaffuri
 * 
 */
public class TEnclaveDomainDeletion extends Transformation<ADomain> {

	public TEnclaveDomainDeletion(ADomain agent) { super(agent); }

	@Override
	public void apply() {
		boolean b;

		Domain dom = agent.getObject();
		Graph g = dom.getGraph();

		agent.setDeleted(true);

		//TODO

		//remove domain from graph
		g.removeDomain(dom);
		/*
		//break link with unit
		b = agent.aUnit.aDomains.remove(agent);
		if(!b) System.err.println("Could not remove domain agent "+agent.getId()+" from tesselation");

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
		}*/
	}

	@Override
	public void storeState() {
		//TODO
	}

	@Override
	public void cancel() {
		//TODO
		System.err.println("cancel() not implemented for "+this.getClass().getSimpleName());
	}

}
