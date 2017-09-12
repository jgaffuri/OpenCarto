/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

/**
 * Ensure the edge face constraint (if any) is satisfied:
 * 
 * @author julien Gaffuri
 *
 */
public class CEdgeFaceSize extends Constraint<AEdge> {
	private final static Logger LOGGER = Logger.getLogger(CEdgeFaceSize.class);

	private CFaceSize sc1, sc2;

	public CEdgeFaceSize(AEdge agent) {
		super(agent);
		//TODO get face size constraints (if any)
	}

	@Override
	public void computeCurrentValue() {
		if(sc1!=null) sc1.computeCurrentValue();
		if(sc2!=null) sc2.computeCurrentValue();
	}


	@Override
	public void computeSatisfaction() {
		if(sc1!=null) sc1.computeSatisfaction();
		if(sc2!=null) sc2.computeSatisfaction();
		//TODO min of both
		satisfaction = 10;
	}

	@Override
	public List<Transformation<AEdge>> getTransformations() {
		return new ArrayList<Transformation<AEdge>>();
	}
}
