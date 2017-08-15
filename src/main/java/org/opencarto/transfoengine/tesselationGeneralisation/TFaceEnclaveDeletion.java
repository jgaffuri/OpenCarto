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
public class TFaceEnclaveDeletion extends Transformation<AFace> {

	public TFaceEnclaveDeletion(AFace agent) { super(agent); }

	@Override
	public void apply() {
		boolean b;

		Face f = agent.getObject();
		Graph g = f.getGraph();

		//delete face, making a hole
		new TFaceIslandDeletion(agent).apply();

		//get edge
		if(f.getEdges().size()!=1) System.err.println("Unexpected number of edges for enclave face "+f.getId());
		Edge e = f.getEdges().iterator().next();
		//get other face
		if(e.getFaces().size()!=1) System.err.println("Unexpected number of faces for edge "+e.getId()+". It should be one but "+e.getFaces().size()+" were found.");
		Face f_ = e.getFaces().iterator().next();

		//remove edge
		b = f_.getEdges().remove(e);
		if(!b) System.err.println("Error when removing edge "+e.getId()+" from face "+f_.getId()+". Not in face edges list.");
		e.f1=null; e.f2=null;
		g.remove(e);
		//remove corresponding agent edge
		agent.getAtesselation().getAEdge(e).setDeleted(true);

		//remove node
		g.remove(f.getNodes().iterator().next());
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
