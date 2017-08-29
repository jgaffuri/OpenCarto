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
		//TODO
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
