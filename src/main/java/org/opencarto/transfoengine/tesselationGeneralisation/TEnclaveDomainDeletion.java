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
		//TODO check nb=1
		Edge e = dom.getEdges().iterator().next();
		//get othe domain
		//TODO check nb=1
		Domain dom_ = e.getDomains().iterator().next();

		dom_.getEdges().remove(e);
		e.d1=null; e.d2=null;
		g.remove(e);
		g.remove(dom.getNodes().iterator().next());

		//TODO finish that - remove hole in surrounding domain.
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
