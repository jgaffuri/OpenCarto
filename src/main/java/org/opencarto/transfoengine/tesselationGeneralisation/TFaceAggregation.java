package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Face;
import org.opencarto.transfoengine.Transformation;

/**
 * @author julien Gaffuri
 *
 */
public class TFaceAggregation extends Transformation<AFace> {
	Face targetFace; Edge edge;

	public TFaceAggregation(AFace aFace, Face targetFace, Edge edge) {
		super(aFace);
		this.targetFace = targetFace;
		this.edge = edge;
	}



	@Override
	public void apply() {
		Face delFace = agent.getObject();

		//TODO remove edge and delFace from graph
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
