/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.Node;
import org.opencarto.transfoengine.Transformation;

/**
 * @author julien Gaffuri
 *
 */
public class TEdgeCollapse extends Transformation<AEdge> {

	public TEdgeCollapse(AEdge agent) { super(agent); }

	//TODO: not safe. It does not ensure that the surounding domains are still valid polygons !

	@Override
	public void apply() {
		Edge e = agent.getObject();
		Graph g = e.getGraph();
		Node n1 = e.getN1(), n2 = e.getN2();

		//merge node 2 into node 1
		n1.getInEdges().addAll(n2.getInEdges());   n2.getInEdges().clear();
		n1.getOutEdges().addAll(n2.getOutEdges());   n2.getOutEdges().clear();

		//move node 1 to edge center
		n1.getC().x = 0.5*(n1.getC().x+n2.getC().x);
		n1.getC().y = 0.5*(n1.getC().y+n2.getC().y);

		//break link edge/domains
		if(e.d1!=null) e.d1.getEdges().remove(e);
		if(e.d2!=null) e.d2.getEdges().remove(e);
		e.d1=null; e.d2=null;

		//delete edge and node 2 from graph
		g.remove(e);
		g.remove(n2);

		//delete edge agent
		agent.setDeleted(true);
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
