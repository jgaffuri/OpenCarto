package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.Set;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Face;
import org.opencarto.datamodel.graph.Graph;
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
		Graph g = delFace.getGraph();

		//aggregate
		Set<Edge> delEdges = g.aggregate(targetFace, delFace);

		//delete agents
		agent.setDeleted(true);
		for(Edge e:delEdges) agent.getAtesselation().getAEdge(e).setDeleted(true);

		//break link with unit
		if(agent.aUnit != null){
			boolean b = agent.aUnit.aFaces.remove(agent);
			if(!b) System.err.println("Could not remove face agent "+agent.getId()+" from tesselation");
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
