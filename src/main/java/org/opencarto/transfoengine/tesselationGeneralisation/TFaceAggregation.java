package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.Set;

import org.apache.log4j.Logger;
import org.opencarto.transfoengine.TransformationNonCancellable;

import eu.europa.ec.eurostat.eurogeostat.algo.graph.FaceAggregation;
import eu.europa.ec.eurostat.eurogeostat.datamodel.graph.Edge;
import eu.europa.ec.eurostat.eurogeostat.datamodel.graph.Face;

/**
 * @author julien Gaffuri
 *
 */
public class TFaceAggregation extends TransformationNonCancellable<AFace> {
	private final static Logger LOGGER = Logger.getLogger(TFaceAggregation.class.getName());

	public TFaceAggregation(AFace agent) { super(agent); }

	@Override
	public void apply() {
		Face delFace = getAgent().getObject();

		Face targetFace = FaceAggregation.getBestAggregationCandidate(delFace);

		if(targetFace == null) {
			LOGGER.error("Null candidate face for aggregation of face "+getAgent().getObject().getId()+". Number of edges: "+getAgent().getObject().getEdges().size());
			return;
		}

		//aggregate
		Set<Edge> delEdges = FaceAggregation.aggregate(targetFace, delFace);
		if(delEdges.size()==0) {
			LOGGER.error("Could not aggregate agent face "+getAgent().getId()+" with face "+targetFace.getId()+": No edge in common.");
			return;
		}

		//delete agents
		getAgent().setDeleted(true);
		for(Edge e:delEdges) getAgent().getAtesselation().getAEdge(e).setDeleted(true);
		//if(getAgent().lastUnitFace()) getAgent().aUnit.setDeleted(true);

		//break link with unit
		if(getAgent().aUnit != null){
			boolean b = getAgent().aUnit.aFaces.remove(getAgent());
			if(!b) LOGGER.error("Could not remove face agent "+getAgent().getId()+" from tesselation");
		}
	}

	public String toString(){
		return getClass().getSimpleName();
	}
}
