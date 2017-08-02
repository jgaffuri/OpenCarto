/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.datamodel.graph.Domain;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.Transformation;

/**
 * 
 * Delete a graph domain
 * 
 * @author julien Gaffuri
 * 
 */
public class TDomainDeletion extends Transformation {

	private Graph graph;

	public TDomainDeletion(Graph graph) {
		this.graph = graph;
	}

	@Override
	public void apply(Agent agent) {
		ADomain domAg = (ADomain)agent;
		Domain dom = domAg.getObject();

		//domAg.setDeleted(true);
		//delete dom in graph
		
		//TODO check if there is a 

	}

}
