/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.datamodel.graph.Domain;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
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

		//delete domain, making a hole
		new TIslandDomainDeletion(agent).apply();

		//get edge
		if(dom.getEdges().size()!=1) System.err.println("Unexpected number of edges for enclave domain "+dom.getId());
		Edge e = dom.getEdges().iterator().next();
		//get other domain
		if(e.getDomains().size()!=1) System.err.println("Unexpected number of domains for edge "+e.getId()+". It should be one but "+e.getDomains().size()+" were found.");
		Domain dom_ = e.getDomains().iterator().next();

		//remove edge
		b = dom_.getEdges().remove(e);
		if(!b) System.err.println("Error when removing edge "+e.getId()+" from domain "+dom_.getId()+". Not in domain edges list.");
		e.d1=null; e.d2=null;
		g.remove(e);

		//remove node
		g.remove(dom.getNodes().iterator().next());
	}


	

	@Override
	public boolean isCancelable() { return false; }

	@Override
	public void storeState() {}

	@Override
	public void cancel() {
		System.err.println("cancel() not implemented for "+this.getClass().getSimpleName());
	}

}
