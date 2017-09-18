/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.transfoengine.Transformation;

/**
 * 
 * Transformation to alter the length of a graph edge.
 * 
 * @author julien Gaffuri
 *
 */
public class TEdgeScale extends Transformation<AEdge> {

	private double factor;
	public TEdgeScale(AEdge agent, double factor) {
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
