package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.transfoengine.Transformation;
import org.opencarto.util.Util;

/**
 * @author julien Gaffuri
 *
 */
public class TFaceScaling extends Transformation<AFace> {
	double factor;

	public TFaceScaling(AFace agent, double factor) {
		super(agent);
		this.factor = factor;
	}



	@Override
	public void apply() {
		//System.out.println("Scaling "+agent.getObject().getGeometry().getCentroid());
		agent.getObject().scale(factor);
	}


	@Override
	public boolean isCancelable() { return true; }

	@Override
	public void storeState() {}

	@Override
	public void cancel() {
		//System.out.println("Undo scaling "+agent.getObject().getGeometry().getCentroid());
		agent.getObject().scale(1/factor);
	}


	public String toString(){
		return getClass().getSimpleName() + "("+Util.round(factor, 3)+")";
	}

}
