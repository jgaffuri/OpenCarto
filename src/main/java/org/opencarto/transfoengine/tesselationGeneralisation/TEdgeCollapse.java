/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.Node;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.Envelope;

/**
 * @author julien Gaffuri
 *
 */
public class TEdgeCollapse extends Transformation<AEdge> {

	public TEdgeCollapse(AEdge agent) { super(agent); }

	//TODO: not safe. It does not ensure that the surounding faces are still valid polygons !

	@Override
	public void apply() {
		Edge e = agent.getObject();
		Graph g = e.getGraph();
		Node n = e.getN1(), n_ = e.getN2();

		//break link edge/faces
		if(e.f1 != null) { e.f1.getEdges().remove(e); e.f1=null; }
		if(e.f2 != null) { e.f2.getEdges().remove(e); e.f2=null; }

		//delete edge from graph
		g.remove(e);

		//move node n to edge center
		g.getSpatialIndexNode().remove(new Envelope(n.getC()), n);
		n.getC().x = 0.5*(n.getC().x+n_.getC().x);
		n.getC().y = 0.5*(n.getC().y+n_.getC().y);
		g.getSpatialIndexNode().insert(new Envelope(n.getC()), n);

		//make node n origin of all edges starting from node n_
		for(Edge e_:n_.getOutEdges()) e_.setN1(n);
		n.getOutEdges().addAll(n_.getOutEdges()); n_.getOutEdges().clear();
		//make node n destination of all edges going to node n_
		for(Edge e_:n_.getInEdges()) e_.setN2(n);
		n.getInEdges().addAll(n_.getInEdges()); n_.getInEdges().clear();

		//delete node n_ from graph
		g.remove(n_);

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
