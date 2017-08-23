package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.Collection;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.Transformation;

public class TFaceHolesDeletion extends Transformation<AFace> {

	private Collection<Edge> tooSmallHoles;

	public TFaceHolesDeletion(AFace agent, Collection<Edge> tooSmallHoles) {
		super(agent);
		this.tooSmallHoles = tooSmallHoles;
	}



	@Override
	public void apply() {
		for(Edge hole : tooSmallHoles){
			//delete hole
			//TODO
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
