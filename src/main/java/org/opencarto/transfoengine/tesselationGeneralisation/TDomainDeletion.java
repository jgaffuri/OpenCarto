/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.datamodel.graph.Domain;
import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.Transformation;

/**
 * 
 * Delete a graph domain. Do not handle case of amalgamation.
 * This operation may result in a hole in the graph tesselation. To prevent that, use aggregation instead.
 * 
 * @author julien Gaffuri
 * 
 */
public class TDomainDeletion extends Transformation {

	@Override
	public void apply(Agent agent) {
		ADomain domAg = (ADomain)agent;
		Domain dom = domAg.getObject();

		//domAg.setDeleted(true);
		//delete dom in graph
		//TODO check if there is a 

	}

}
