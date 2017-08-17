package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Face;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.transfoengine.Transformation;

/**
 * @author julien Gaffuri
 *
 */
public class TFaceAggregation extends Transformation<AFace> {
	Face targetFace; Edge delEdge;

	public TFaceAggregation(AFace agent, Face targetFace, Edge delEdge) {
		super(agent);
		this.targetFace = targetFace;
		this.delEdge = delEdge;
	}



	@Override
	public void apply() {
		Face delFace = agent.getObject();
		Graph g = delFace.getGraph();

		//remove edge
		delEdge.f1=null; delEdge.f2=null;
		targetFace.getEdges().remove(delEdge);
		delFace.getEdges().remove(delEdge);
		g.remove(delEdge);

		//remove edge agent
		AEdge ea = agent.getAtesselation().getAEdge(delEdge);
		if(ea==null) System.err.println("Could not find edge agent for edge "+delEdge.getId());
		else ea.setDeleted(true);

		//aggregate faces
		for(Edge e : delFace.getEdges()) if(e.f1==delFace) e.f1=targetFace; else e.f2=targetFace;
		targetFace.getEdges().addAll(delFace.getEdges());
		delFace.getEdges().clear();
		g.removeFace(delFace);

		//delete agent face
		agent.setDeleted(true);

		//case of enclave deletion: delete also the remaining node
		if(delEdge.isClosed()) g.remove(delEdge.getN1());

		//ensure nodes are reduced, which means they do not have a degree 2
		Edge e1 = delEdge.getN1().ensureReduction();
		Edge e2 = delEdge.getN2().ensureReduction();
		//TODO create edge agents?
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
