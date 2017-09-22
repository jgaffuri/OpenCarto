package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.Collection;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.transfoengine.TransformationNonCancellable;

/**
 * @author julien Gaffuri
 *
 */
public class TFaceHolesDeletion extends TransformationNonCancellable<AFace> {

	private Collection<Edge> tooSmallHoles;

	public TFaceHolesDeletion(AFace agent, Collection<Edge> tooSmallHoles) {
		super(agent);
		this.tooSmallHoles = tooSmallHoles;
	}


	@Override
	public void apply() {
		System.out.println("check if used ! "+getClass().getSimpleName());

		for(Edge e : tooSmallHoles){
			Graph g = e.getGraph();

			//break link with face
			e.breakLinkWithFace(getAgent().getObject());

			//remove edge and corresponding node
			g.remove(e); g.remove(e.getN1());

			//delete corresponding edge agent
			getAgent().getAtesselation().getAEdge(e).setDeleted(true);

			//TODO remove also potential islands in hole?
		}
	}



	public String toString(){
		return getClass().getSimpleName() + "(nb="+tooSmallHoles.size()+")";
	}
}
