/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.datamodel.graph.Face;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.transfoengine.Transformation;

/**
 * 
 * @author julien Gaffuri
 * 
 */
public class TEnclaveDomainDeletion extends Transformation<AFace> {

	public TEnclaveDomainDeletion(AFace agent) { super(agent); }

	@Override
	public void apply() {
		boolean b;

		Face dom = agent.getObject();
		Graph g = dom.getGraph();

		//delete domain, making a hole
		new TIslandDomainDeletion(agent).apply();

		//get edge
		if(dom.getEdges().size()!=1) System.err.println("Unexpected number of edges for enclave domain "+dom.getId());
		Edge e = dom.getEdges().iterator().next();
		//get other domain
		if(e.getFaces().size()!=1) System.err.println("Unexpected number of domains for edge "+e.getId()+". It should be one but "+e.getFaces().size()+" were found.");
		Face dom_ = e.getFaces().iterator().next();

		//remove edge
		b = dom_.getEdges().remove(e);
		if(!b) System.err.println("Error when removing edge "+e.getId()+" from domain "+dom_.getId()+". Not in domain edges list.");
		e.f1=null; e.f2=null;
		g.remove(e);
		//remove corresponding agent edge
		agent.getAtesselation().getAEdge(e).setDeleted(true);

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
