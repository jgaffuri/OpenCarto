package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.algo.graph.FaceScaling;
import org.opencarto.transfoengine.TransformationCancellable;
import org.opencarto.util.Util;

/**
 * @author julien Gaffuri
 *
 */
public class TFaceScaling extends TransformationCancellable<AFace> {
	double factor;

	public TFaceScaling(AFace agent, double factor) {
		super(agent);
		this.factor = factor;
	}



	@Override
	public void apply() {
		//System.out.println("Scaling "+agent.getObject().getGeometry().getCentroid());
		FaceScaling.scale(getAgent().getObject(), factor);
	}


	@Override
	public boolean isCancelable() { return true; }

	@Override
	public void storeState() {}

	@Override
	public void cancel() {
		//System.out.println("Undo scaling "+agent.getObject().getGeometry().getCentroid());
		FaceScaling.scale(getAgent().getObject(), 1/factor);
	}


	public String toString(){
		return getClass().getSimpleName() + "("+Util.round(factor, 3)+")";
	}

}
