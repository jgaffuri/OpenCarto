package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.Collection;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.Node;
import org.opencarto.transfoengine.Transformation;

public class TFaceHolesDeletion extends Transformation<AFace> {

	private Collection<Edge> tooSmallHoles;

	public TFaceHolesDeletion(AFace agent, Collection<Edge> tooSmallHoles) {
		super(agent);
		this.tooSmallHoles = tooSmallHoles;
	}



	@Override
	public void apply() {
		for(Edge e : tooSmallHoles){
			Graph g = e.getGraph();
			Node n = e.getN1();

			//delete edge
			e.f1=null; e.f2=null;
			g.remove(e);

			//delete node
			g.remove(n);

			//delete corresponding edge agent
			agent.getAtesselation().getAEdge(e).setDeleted(true);
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
