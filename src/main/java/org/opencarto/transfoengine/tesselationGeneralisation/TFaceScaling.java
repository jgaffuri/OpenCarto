package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.transfoengine.Transformation;

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
		agent.getObject().scale(factor);
	}


	@Override
	public boolean isCancelable() { return true; }

	@Override
	public void storeState() {}

	@Override
	public void cancel() {
		agent.getObject().scale(1/factor);
	}

}
