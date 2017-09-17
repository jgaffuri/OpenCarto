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
public class TEdgeChangeLength extends Transformation<AEdge> {
	private final double ZERO_FACTOR = 0.000001;

	private double factor;
	public TEdgeChangeLength(AEdge agent, double factor) {
		super(agent);
		this.factor = factor;
		if (this.factor<ZERO_FACTOR) this.factor = ZERO_FACTOR;
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
