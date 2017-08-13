/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.datamodel.graph.Domain;
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

		new TIslandDomainDeletion(agent).apply();

		//TODO finish that
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
