package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.Set;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Face;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.transfoengine.Transformation;

/**
 * @author julien Gaffuri
 *
 */
public class TFaceAggregation extends Transformation<AFace> {
	private final static Logger LOGGER = Logger.getLogger(TFaceAggregation.class);

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
		if(delEdges.size()==0) {
			LOGGER.error("Could not aggregate agent face "+agent.getId()+" with face "+targetFace.getId()+": No edge in common.");
			return;
		}

		//delete agents
		agent.setDeleted(true);
		for(Edge e:delEdges) agent.getAtesselation().getAEdge(e).setDeleted(true);

		//break link with unit
		if(agent.aUnit != null){
			boolean b = agent.aUnit.aFaces.remove(agent);
			if(!b) LOGGER.error("Could not remove face agent "+agent.getId()+" from tesselation");
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

	public String toString(){
		return getClass().getSimpleName() + "(target="+targetFace.getId()+")";
	}
}
