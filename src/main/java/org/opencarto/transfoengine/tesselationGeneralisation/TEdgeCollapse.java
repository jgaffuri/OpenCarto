/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.algo.graph.EdgeCollapse;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.TransformationNonCancellable;

/**
 * @author julien Gaffuri
 *
 */
public class TEdgeCollapse extends TransformationNonCancellable<AEdge> {

	public TEdgeCollapse(AEdge agent) { super(agent); }

	//TODO: not safe. It does not ensure that the surounding faces are still valid polygons !

	@Override
	public void apply() {
		Edge e = getAgent().getObject();

		//collapse edge
		EdgeCollapse.collapseEdge(e);

		//delete edge agent
		getAgent().setDeleted(true);
	}

}
