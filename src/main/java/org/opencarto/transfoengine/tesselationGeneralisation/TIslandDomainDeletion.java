/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.Collection;

import org.opencarto.datamodel.graph.Face;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.Node;
import org.opencarto.transfoengine.Transformation;

/**
 * 
 * Delete a graph domain. It should be used only to remove island domains.
 * Otherwise, this operation may result in a hole in the graph tesselation.
 * The edges and nodes which are not linked anymore to any other graph element are also deleted
 * 
 * @author julien Gaffuri
 * 
 */
public class TIslandDomainDeletion extends Transformation<AFace> {

	public TIslandDomainDeletion(AFace agent) { super(agent); }

	@Override
	public void apply() {
		boolean b;

		Face dom = agent.getObject();
		Graph g = dom.getGraph();

		agent.setDeleted(true);

		//remove domain from graph
		g.removeFace(dom);

		//break link with unit
		b = agent.aUnit.aFaces.remove(agent);
		if(!b) System.err.println("Could not remove domain agent "+agent.getId()+" from tesselation");

		//remove useless edges
		Collection<Edge> es = dom.getEdges();
		for(Edge e:es){
			if(e.getFaces().size()>0) continue;
			g.remove(e);
			agent.getAtesselation().getAEdge(e).setDeleted(true);
		}

		//remove useless nodes
		Collection<Node> ns = dom.getNodes();
		for(Node n:ns){
			if(n.getFaces().size()>0) continue;
			g.remove(n);
		}
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
