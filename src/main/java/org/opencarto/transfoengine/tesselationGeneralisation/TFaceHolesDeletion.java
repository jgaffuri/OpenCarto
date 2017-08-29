package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.Collection;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.transfoengine.Transformation;

public class TFaceHolesDeletion extends Transformation<AFace> {

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
			e.breakLinkWithFace(agent.getObject());

			//remove edge and corresponding node
			g.remove(e); g.remove(e.getN1());

			//delete corresponding edge agent
			agent.getAtesselation().getAEdge(e).setDeleted(true);

			//TODO remove also potential islands in hole?
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
