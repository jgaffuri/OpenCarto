package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.Set;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Face;
import org.opencarto.transfoengine.Transformation;

/**
 * @author julien Gaffuri
 *
 */
public class TFaceAggregation extends Transformation<AFace> {
	Face targetFace;

	public TFaceAggregation(AFace agent, Face targetFace) {
		super(agent);
		this.targetFace = targetFace;
	}



	@Override
	public void apply() {
		Face delFace = agent.getObject();
		//Graph g = delFace.getGraph();

		Set<Edge> delEdges = targetFace.absorb(delFace);

		//delete agents
		agent.setDeleted(true);
		for(Edge e:delEdges) agent.getAtesselation().getAEdge(e).setDeleted(true);

		//TODO
		//ensure nodes are reduced, which means they do not have a degree 2
		//Edge e1 = delEdge.getN1().ensureReduction();
		//Edge e2 = delEdge.getN2().ensureReduction();
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
