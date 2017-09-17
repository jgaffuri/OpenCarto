/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.apache.commons.math3.genetics.GeneticAlgorithm;
import org.opencarto.transfoengine.Transformation;

/**
 * 
 * Transformation to alter the length of a graph edge.
 * 
 * @author julien Gaffuri
 *
 */
public class TEdgeChangeLength extends Transformation<AEdge> {
	private final double ZERO_K = 0.000001;

	private double k;
	public TEdgeChangeLength(AEdge agent, double k) {
		super(agent);
		this.k = k;
		if (this.k<ZERO_K) this.k = ZERO_K;
	}

	@Override
	public void apply() {
		agent.getObject().changeLength(k);
	}



	@Override
	public boolean isCancelable() { return true; }

	@Override
	public void storeState() {}

	@Override
	public void cancel() {
		agent.getObject().changeLength(1/k);
	}

}
